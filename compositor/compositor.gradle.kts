/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

import org.jetbrains.kotlin.gradle.tasks.CInteropProcess

plugins {
    // version inherited from the root build classpath (catalog "kotlin");
    // catalog aliases are not usable here because of the buildSrc project
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

// typed `libs` accessors are not generated in this script (buildSrc clash),
// the values still come from gradle/libs.versions.toml via the catalog api
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
group = catalog.findVersion("app-pkg-name").get().requiredVersion
version = catalog.findVersion("app-pkg-version").get().requiredVersion

val nativeDir = project.file("native")
val protocolsDir = layout.buildDirectory.dir("generated/protocols")
val shimDir = layout.buildDirectory.dir("shim")

// pkg-config is queried lazily and never fails configuration,
// so the JVM desktop build stays usable on machines without wlroots
fun pkgConfig(vararg args: String): List<String> = runCatching {
    val process = ProcessBuilder(listOf("pkg-config") + args)
        .redirectErrorStream(false)
        .start()
    val out = process.inputStream.bufferedReader().readText()
    if (process.waitFor() != 0) emptyList()
    else out.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
}.getOrElse { emptyList() }

val wlrPackages = arrayOf("wlroots-0.18", "wayland-server", "xkbcommon", "pixman-1")
val wlrCflags: List<String>
    get() = pkgConfig("--cflags", *wlrPackages)
val wlrLibs: List<String>
    get() = pkgConfig("--libs", *wlrPackages).ifEmpty {
        listOf("-lwlroots-0.18", "-lwayland-server", "-lxkbcommon")
    }

// the kotlin/native bundled linker does not search the system lib dirs
val systemLibDirs = listOf(
    "/usr/lib/x86_64-linux-gnu",
    "/usr/lib",
    "/usr/local/lib",
).filter { file(it).isDirectory }.map { "-L$it" }

val generateProtocols = tasks.register<Exec>("generateProtocols") {
    group = "mjdev"
    description = "Generates wayland protocol headers used by the compositor shim."
    val outDir = protocolsDir.get().asFile
    val xdgShellXml = "/usr/share/wayland-protocols/stable/xdg-shell/xdg-shell.xml"
    inputs.files(xdgShellXml).optional()
    outputs.dir(outDir)
    commandLine(
        "bash", "-c",
        "mkdir -p '$outDir' && " +
                "wayland-scanner server-header '$xdgShellXml' '$outDir/xdg-shell-protocol.h'"
    )
}

val compileShim = tasks.register<Exec>("compileShim") {
    group = "mjdev"
    description = "Compiles the C shim over wlroots into a static library."
    dependsOn(generateProtocols)
    inputs.files(nativeDir.resolve("shim.c"), nativeDir.resolve("shim.h"))
    outputs.file(shimDir.map { it.file("libmjcshim.a") })
    // locals only - the doFirst closure must not capture the script object,
    // the configuration cache cannot serialize script references
    val out = shimDir.get().asFile
    val protoDir = protocolsDir.get().asFile
    val srcDir = nativeDir
    val cflags = wlrCflags.joinToString(" ")
    doFirst {
        commandLine(
            "bash", "-c",
            "mkdir -p '$out' && " +
                    "cc -O2 -fPIC -DWLR_USE_UNSTABLE -std=c11 " +
                    "-I'$protoDir' -I'$srcDir' $cflags " +
                    "-c '${srcDir.resolve("shim.c")}' -o '$out/shim.o' && " +
                    "ar rcs '$out/libmjcshim.a' '$out/shim.o'"
        )
    }
    // placeholder, replaced in doFirst (Exec requires a commandLine at configuration)
    commandLine("true")
}

kotlin {
    linuxX64 {
        compilations.getByName("main") {
            cinterops.create("shim") {
                defFile(nativeDir.resolve("shim.def"))
                packageName("mjdev.compositor.shim")
                includeDirs(nativeDir)
            }
        }
        binaries {
            executable("mjdevc") {
                entryPoint = "eu.mjdev.compositor.main"
                // link against the system glibc instead of the old bundled
                // sysroot, system libs (wlroots & co) need modern symbols
                val gccVersion = file("/usr/lib/gcc/x86_64-linux-gnu")
                    .listFiles()?.map { it.name }?.maxByOrNull { it.toIntOrNull() ?: 0 }
                if (gccVersion != null) {
                    freeCompilerArgs += listOf(
                        "-Xoverride-konan-properties=" +
                                "targetSysRoot.linux_x64=/;" +
                                "libGcc.linux_x64=/usr/lib/gcc/x86_64-linux-gnu/$gccVersion;" +
                                "crtFilesLocation.linux_x64=usr/lib/x86_64-linux-gnu"
                    )
                }
                linkerOpts(systemLibDirs)
                linkerOpts(shimDir.get().asFile.resolve("libmjcshim.a").absolutePath)
                linkerOpts(wlrLibs)
            }
        }
    }

    sourceSets {
        getByName("linuxX64Main") {
            dependencies {
                implementation(
                    catalog.findLibrary("kotlinx-serialization-json").get()
                )
            }
        }
    }
}

// the static shim library must exist before linking the executable
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink>().configureEach {
    dependsOn(compileShim)
}
// note: cinterop parses only the self contained shim.h,
// generated wayland protocol headers are needed just for compileShim

tasks.register<Copy>("stageSession") {
    group = "mjdev"
    description = "Stages the compositor binary and session files for installation."
    dependsOn("linkMjdevcReleaseExecutableLinuxX64")
    from(layout.buildDirectory.file("bin/linuxX64/mjdevcReleaseExecutable/mjdevc.kexe")) {
        rename { "mjdevc" }
    }
    from(rootProject.file("session/mjdev-session"))
    from(rootProject.file("session/mjdev.desktop"))
    from(rootProject.file("session/mjdev-desktop.service"))
    from(nativeDir.resolve("install.sh"))
    into(layout.buildDirectory.dir("session-install"))
}

tasks.register("installSession") {
    group = "mjdev"
    description = "Prints the command installing the mjdev wayland session (needs sudo)."
    dependsOn("stageSession")
    // local only - the doLast closure must not capture the script object
    val dir = layout.buildDirectory.dir("session-install").get().asFile
    doLast {
        println("Session staged. Install it with:")
        println("  sudo sh '$dir/install.sh'")
    }
}

tasks.register<Exec>("runNested") {
    group = "mjdev"
    description = "Runs the compositor nested inside the current Wayland session."
    dependsOn("linkMjdevcDebugExecutableLinuxX64")
    val shellCmd = (project.findProperty("shellCmd") as String?)
    // local only - the doFirst closure must not capture the script object
    val binary = layout.buildDirectory
        .file("bin/linuxX64/mjdevcDebugExecutable/mjdevc.kexe")
        .get().asFile.absolutePath
    doFirst {
        val args = mutableListOf(binary)
        if (shellCmd != null) {
            args += listOf("--shell-cmd", shellCmd)
        }
        commandLine(args)
    }
    commandLine("true")
}

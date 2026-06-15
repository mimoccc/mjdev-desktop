/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

import org.jetbrains.kotlin.gradle.tasks.CInteropProcess

plugins {
    // kotlin + serialization come from the root build classpath (declared apply-false there)
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

// identity from the shared version catalog (gradle/libs.versions.toml)
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
    // capture everything as local values at configuration time so the doFirst closure
    // never reaches back into the build script (required for configuration-cache safety)
    val shimC = nativeDir.resolve("shim.c")
    val shimH = nativeDir.resolve("shim.h")
    val outDir = shimDir.get().asFile
    val protoDir = protocolsDir.get().asFile
    val includeDir = nativeDir
    val cflags = wlrCflags.joinToString(" ")
    inputs.files(shimC, shimH)
    outputs.file(outDir.resolve("libmjcshim.a"))
    doFirst {
        commandLine(
            "bash", "-c",
            "mkdir -p '$outDir' && " +
                    "cc -O2 -fPIC -DWLR_USE_UNSTABLE -std=c11 " +
                    "-I'$protoDir' -I'$includeDir' $cflags " +
                    "-c '$shimC' -o '$outDir/shim.o' && " +
                    "ar rcs '$outDir/libmjcshim.a' '$outDir/shim.o'"
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
                implementation(catalog.findLibrary("kotlinx-serialization-json").get())
            }
        }
    }
}

// the static shim library must exist before linking the executable; it is passed via
// linkerOpts (an absolute path), which Kotlin/Native does not track — so register it as an
// explicit input, otherwise editing shim.c rebuilds the .a but never relinks the binary
// (you would keep running a stale executable).
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink>().configureEach {
    dependsOn(compileShim)
    inputs.file(shimDir.map { it.file("libmjcshim.a") })
        .withPropertyName("mjcShimLibrary")
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
    into(layout.buildDirectory.dir("session-install"))
}

// Privileged install steps for the compositor + wayland session. Installs the binary, the
// session launcher and the wayland-sessions entry, and removes the stale 2024 X11
// "mjdev-desktop" session that shadows ours (same display name) and breaks login. Lines are
// built at configuration time (staged paths are static) so the Exec stays config-cache safe.
// built at configuration time (staged paths are static) so referencing the result from doLast
// stays configuration-cache safe (the function itself is never captured in the task action).
// the wayland runtime stack mjdevc needs — from the version catalog (single source of truth,
// shared with the deb Depends and make-iso.sh), never hardcoded here.
val compositorRuntimeDeps: String = libs.versions.app.compositor.runtime.deps.get()

fun sessionInstallLines(staged: File): List<String> = listOf(
    "#!/bin/sh",
    "set -e",
    // runtime stack the compositor needs but a clean / GNOME box lacks (GNOME uses mutter,
    // not wlroots). dpkg can't resolve these, so apt-install them explicitly. libwlroots-0.18
    // pulls libdrm/libgbm/libinput/libseat/libxkbcommon/libwayland/libdisplay-info/libliftoff;
    // xwayland = X display for the AWT-based Compose shell; libgl1-mesa-dri = the GL/EGL driver
    // (incl. llvmpipe software fallback). Without these mjdevc fails to even load -> black screen.
    "apt-get update || true",
    "apt-get install --no-install-recommends -y $compositorRuntimeDeps seatd || " +
            "echo 'WARN: apt could not install the wayland runtime stack (offline or non-debian?) — the desktop may not start'",
    // the compositor opens /dev/dri/card0 + the seatd socket (group video); seatd must run and
    // the logged-in user must be in video/input/render (+ seat if present) or the session is black.
    "systemctl enable --now seatd 2>/dev/null || true",
    "_u=\"\$(getent passwd \"\${PKEXEC_UID:-\${SUDO_UID:-1000}}\" | cut -d: -f1)\"",
    "[ -n \"\$_u\" ] && usermod -aG video,input,render \"\$_u\" 2>/dev/null || true",
    "getent group seat >/dev/null 2>&1 && [ -n \"\$_u\" ] && usermod -aG seat \"\$_u\" 2>/dev/null || true",
    "install -Dm755 '${staged.resolve("mjdevc")}' /usr/local/bin/mjdevc",
    "install -Dm755 '${staged.resolve("mjdev-session")}' /usr/local/bin/mjdev-session",
    "install -Dm644 '${staged.resolve("mjdev.desktop")}' /usr/share/wayland-sessions/mjdev.desktop",
    "rm -f /usr/share/xsessions/mjdev-desktop.desktop " +
            "/usr/share/gnome-session/sessions/mjdev-desktop.session " +
            "/usr/share/applications/mjdev-desktop-mjdev-desktop.desktop",
)

// sudo can't read a password from the Gradle daemon (no TTY), so these tasks don't exec it —
// they stage everything, write a self-contained install script under build/, and print the one
// sudo command for the user to run in their own terminal.
tasks.register("installSession") {
    group = "mjdev"
    description = "Stages the compositor + wayland session, prints the sudo install command."
    dependsOn("stageSession")
    val staged = layout.buildDirectory.dir("session-install").get().asFile
    val lines = sessionInstallLines(staged)
    doLast {
        val script = staged.resolve("install.sh")
        script.writeText(lines.joinToString("\n") + "\n")
        println("Session staged. Install it (single sudo prompt) with:")
        println("  sudo sh '${script.absolutePath}'")
    }
}

// Complete (re)install: compositor + wayland session + the desktop app .deb (the session's
// shell, /opt/mjdev-desktop), and the stale-session cleanup — one script run as root via a
// pkexec (polkit) authentication dialog. pkexec needs no TTY (unlike sudo), so it works from
// the detached Gradle daemon and prompts graphically; if it's missing we print the sudo command.
tasks.register<Exec>("installDesktop") {
    group = "mjdev"
    description = "Builds compositor + session + app .deb and installs it via a pkexec authentication dialog."
    dependsOn("stageSession", ":desktopApp:packageReleaseDeb", ":packageFullDeb")
    val staged = layout.buildDirectory.dir("session-install").get().asFile
    val lines = sessionInstallLines(staged)
    // desktopApp overrides compose outputBaseDir to <root>/packages, so the .deb is written
    // to packages/main-release/deb (not desktopApp/build/compose/binaries/…)
    val debDir = rootProject.rootDir.resolve("packages/main-release/deb")
    isIgnoreExitValue = false
    doFirst {
        val deb = debDir.listFiles { f -> f.extension == "deb" }?.firstOrNull()
            ?: error("desktop .deb not found in $debDir — run :desktopApp:packageReleaseDeb")
        val script = staged.resolve("install-desktop.sh")
        // install the deb via apt so its Depends (the wayland runtime stack baked in by
        // packageFullDeb) are resolved; fall back to dpkg + apt -f if the apt form is unavailable.
        val installDeb = "apt-get install --no-install-recommends -y '${deb.absolutePath}' || " +
                "{ dpkg -i '${deb.absolutePath}' || true; apt-get install -f -y; }"
        script.writeText((lines + installDeb).joinToString("\n") + "\n")
        // pkexec pops a graphical polkit auth dialog and runs the script as root; needs a polkit
        // agent in the session but no terminal. Fall back to a printed sudo command if absent.
        val pkexec = listOf("/usr/bin/pkexec", "/usr/local/bin/pkexec")
            .firstOrNull { File(it).canExecute() }
        if (pkexec != null) {
            println("Installing the mjdev desktop — approve the authentication dialog…")
            commandLine(pkexec, "sh", script.absolutePath)
        } else {
            println("pkexec not found. Install the desktop (single sudo prompt) with:")
            println("  sudo sh '${script.absolutePath}'")
            commandLine("true")
        }
    }
    commandLine("true")
}

tasks.register<Exec>("runNested") {
    group = "mjdev"
    description = "Runs the compositor nested inside the current Wayland session."
    dependsOn("linkMjdevcDebugExecutableLinuxX64")
    val shellCmd = (project.findProperty("shellCmd") as String?)
    val binary = layout.buildDirectory
        .file("bin/linuxX64/mjdevcDebugExecutable/mjdevc.kexe")
        .get().asFile.absolutePath
    // x11 backend -> the host window manager decorates the nested window (frame + close +
    // move); 16:9 nested output (the compositor locks it against resize)
    environment("WLR_BACKENDS", "x11")
    environment("MJDEVC_OUTPUT", "1280x720")
    doFirst {
        // host X11 auth for the nested wlroots x11 backend, resolved at execution time:
        // the detached Gradle daemon often lacks a valid XAUTHORITY, and mutter writes a
        // per-session, randomly-suffixed auth file under XDG_RUNTIME_DIR
        environment("DISPLAY", System.getenv("DISPLAY") ?: ":0")
        val xauthCandidates = buildList {
            System.getenv("XAUTHORITY")?.let { add(File(it)) }
            System.getenv("XDG_RUNTIME_DIR")?.let { dir ->
                File(dir).listFiles()?.forEach { f ->
                    if (f.name.startsWith(".mutter-Xwaylandauth.")) add(f)
                }
            }
            add(File(System.getProperty("user.home"), ".Xauthority"))
        }
        xauthCandidates.firstOrNull { it.isFile }?.let { environment("XAUTHORITY", it.absolutePath) }
        val args = mutableListOf(binary)
        if (shellCmd != null) {
            args += listOf("--shell-cmd", shellCmd)
        }
        commandLine(args)
    }
    commandLine("true")
}

// Runs the Compose desktop app as the shell client inside the nested compositor.
// The desktop app is packaged with its own JRE (createDistributable) and launched via
// XWayland, so it shows up as a window managed by mjdevc (fixed 16:9, movable, closable).
tasks.register<Exec>("runNestedDesktop") {
    group = "mjdev"
    description = "Runs the mjdev desktop app as a client inside the nested compositor."
    dependsOn("linkMjdevcDebugExecutableLinuxX64", ":desktopApp:createDistributable")
    val binary = layout.buildDirectory
        .file("bin/linuxX64/mjdevcDebugExecutable/mjdevc.kexe")
        .get().asFile.absolutePath
    // desktopApp overrides outputBaseDir to <root>/packages, so createDistributable
    // writes the app image to packages/main/app/<name>/bin/<name> (not build/compose/…)
    val appDir = rootProject.rootDir.resolve("packages/main/app")
    // x11 backend -> host-decorated nested window (frame + close + move); fixed 16:9 output
    environment("WLR_BACKENDS", "x11")
    environment("MJDEVC_OUTPUT", "1280x720")
    doFirst {
        // host X11 auth for the nested wlroots x11 backend, resolved at execution time:
        // the detached Gradle daemon often lacks a valid XAUTHORITY, and mutter writes a
        // per-session, randomly-suffixed auth file under XDG_RUNTIME_DIR
        environment("DISPLAY", System.getenv("DISPLAY") ?: ":0")
        val xauthCandidates = buildList {
            System.getenv("XAUTHORITY")?.let { add(File(it)) }
            System.getenv("XDG_RUNTIME_DIR")?.let { dir ->
                File(dir).listFiles()?.forEach { f ->
                    if (f.name.startsWith(".mutter-Xwaylandauth.")) add(f)
                }
            }
            add(File(System.getProperty("user.home"), ".Xauthority"))
        }
        xauthCandidates.firstOrNull { it.isFile }?.let { environment("XAUTHORITY", it.absolutePath) }
        val launcher = appDir.walkTopDown()
            .firstOrNull { it.isFile && it.parentFile?.name == "bin" && it.canExecute() }
            ?: error("desktop launcher not found under $appDir — run :desktopApp:createDistributable")
        commandLine(binary, "--shell-cmd", launcher.absolutePath)
    }
    commandLine("true")
}

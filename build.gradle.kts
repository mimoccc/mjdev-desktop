plugins {
//    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.app.icon.generator) apply false
    // auto applied
//    alias(libs.plugins.qodana)
//    alias(libs.plugins.kover)
    alias(libs.plugins.changelog) apply false
    alias(libs.plugins.gradle.ktlint)
    alias(libs.plugins.manes.versions) apply false
    alias(libs.plugins.kotlin.android) apply false
//    id("com.github.gmazzo.buildconfig") version "5.6.5"
}

//buildConfig {
//    generateAtSync = true
//    sourceSets  {
//        main {
//            packageName = "org.mjdev.desktop"
//            useKotlin = true
//            useJava = true
//            useGroovy = true
//            useScala = true
//            useXml = true
//            useProperties = true
//        }
//    }
//    buildConfigField("APP_NAME", project.name)
//    buildConfigField("APP_VERSION", provider { "\"${project.version}\"" })
//    buildConfigField("APP_SECRET", "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu")
//    buildConfigField<String>("OPTIONAL", null)
//    buildConfigField("BUILD_TIME", System.currentTimeMillis())
//    buildConfigField("FEATURE_ENABLED", true)
//    buildConfigField("MAGIC_NUMBERS", intArrayOf(1, 2, 3, 4))
//    buildConfigField("STRING_LIST", arrayOf("a", "b", "c"))
//    buildConfigField("MAP", mapOf("a" to 1, "b" to 2))
//    buildConfigField("FILE", File("aFile"))
//    buildConfigField("URI", uri("https://example.io"))
//    buildConfigField("com.github.gmazzo.buildconfig.demos.kts.SomeData", "DATA", "SomeData(\"a\", 1)")
//}

//dependencies {
//    kover(project(":composeApp"))
//}

//kover { reports { total { xml { onCheck = true } } } }

//<editor-fold desc="mjdev run tasks">---------------------------------------------------------------

tasks.register("runDesktop") {
    group = "mjdev"
    description = "Run the desktop app."
    dependsOn(":composeApp:run")
}

tasks.register("runDebug") {
    group = "mjdev"
    description = "Run the desktop app with debug parameter."
    dependsOn(":composeApp:run")
}

// runDebug passes --debug to the composeApp run task
project(":composeApp").tasks.matching { task -> task.name == "run" }.configureEach {
    if (gradle.startParameter.taskNames.any { name -> name.contains("runDebug") }) {
        (this as JavaExec).args("--debug")
    }
}

// installs the debug build on the connected device/emulator and launches it
tasks.register("runAndroid") {
    group = "mjdev"
    description = "Install and run the android app on a connected device."
    dependsOn(":composeApp:installDebug")
    val sdkDir = System.getenv("ANDROID_HOME")
        ?: System.getenv("ANDROID_SDK_ROOT")
        ?: "${System.getProperty("user.home")}/Android/Sdk"
    val appId = libs.versions.app.pkg.name.get()
    doLast {
        val process = ProcessBuilder(
            "$sdkDir/platform-tools/adb", "shell", "am", "start",
            "-n", "$appId/.activity.MainActivity"
        ).inheritIO().start()
        check(process.waitFor() == 0) { "adb launch failed" }
    }
}

// runs the whole desktop nested in a window inside the current wayland
// session - testing without logging out; the compositor starts the
// distributable desktop binary as its shell
tasks.register("runNested") {
    group = "mjdev"
    description = "Run compositor + desktop nested inside the current session."
    dependsOn(":composeApp:createDistributable", ":compositor:linkMjdevcDebugExecutableLinuxX64")
    val appName = libs.versions.app.name.get()
    val shellBinary = rootDir.resolve("packages/main/app/$appName/bin/$appName")
    val compositorBinary = project(":compositor").layout.buildDirectory
        .file("bin/linuxX64/mjdevcDebugExecutable/mjdevc.kexe").get().asFile
    doLast {
        val process = ProcessBuilder(
            compositorBinary.absolutePath,
            "--shell-cmd", shellBinary.absolutePath
        ).inheritIO().start()
        check(process.waitFor() == 0) { "nested compositor exited with error" }
    }
}

//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="mjdev packaging tasks">---------------------------------------------------------

// single deb carrying the desktop shell, the mjdevc compositor and the
// wayland session files - after `apt install` the session is ready on gdm
tasks.register("packageMjdevDeb") {
    group = "mjdev"
    description = "Builds one deb with the desktop, the compositor and the wayland session."
    dependsOn(":composeApp:createDistributable", ":compositor:linkMjdevcReleaseExecutableLinuxX64")
    // all values are captured as locals at configuration time, the doLast
    // closure must not capture the script object (configuration cache)
    val appName = libs.versions.app.name.get()
    val version = libs.versions.app.pkg.version.get()
    val appDescription = libs.versions.app.description.get()
    // catalog format is "email<Name>", debian control wants "Name <email>"
    val maintainer = Regex("(.+)<(.+)>").find(libs.versions.app.maintainer.get())
        ?.let { "${it.groupValues[2].trim()} <${it.groupValues[1].trim()}>" }
        ?: libs.versions.app.maintainer.get()
    val stage = layout.buildDirectory.dir("deb/$appName").get().asFile
    val appImage = rootDir.resolve("packages/main/app/$appName")
    val compositorBinary = project(":compositor").layout.buildDirectory
        .file("bin/linuxX64/mjdevcReleaseExecutable/mjdevc.kexe").get().asFile
    val sessionDir = rootDir.resolve("session")
    val debFile = rootDir.resolve("release/$appName-$version.deb")
    doLast {
        // ProcessBuilder on purpose, Task.exec {} and script helpers are
        // not usable with the configuration cache
        fun run(vararg args: String) {
            val process = ProcessBuilder(*args).inheritIO().start()
            check(process.waitFor() == 0) { "command failed: ${args.joinToString(" ")}" }
        }
        stage.deleteRecursively()

        appImage.copyRecursively(stage.resolve("opt/$appName"), overwrite = true)
        compositorBinary.copyTo(stage.resolve("usr/bin/mjdevc"), overwrite = true)
        sessionDir.resolve("mjdev-session")
            .copyTo(stage.resolve("usr/bin/mjdev-session"), overwrite = true)
        sessionDir.resolve("mjdev.desktop")
            .copyTo(stage.resolve("usr/share/wayland-sessions/mjdev.desktop"), overwrite = true)
        // user unit for autostart without a display manager (kiosk/embedded)
        sessionDir.resolve("mjdev-desktop.service")
            .copyTo(stage.resolve("usr/lib/systemd/user/mjdev-desktop.service"), overwrite = true)

        val installedKb = stage.walkTopDown()
            .filter { it.isFile }.sumOf { it.length() } / 1024
        val control = stage.resolve("DEBIAN/control")
        control.parentFile.mkdirs()
        control.writeText(
            """
            Package: $appName
            Version: $version
            Section: x11
            Priority: optional
            Architecture: amd64
            Installed-Size: $installedKb
            Maintainer: $maintainer
            Depends: libwlroots-0.18, xwayland, libxkbcommon0, libasound2t64 | libasound2, libx11-6, libxext6, libxi6, libxrender1, libxtst6, libfreetype6
            Description: $appDescription
             Wayland desktop environment with its own compositor (mjdevc),
             desktop shell, panel, application menu and control center.
            """.trimIndent() + "\n"
        )

        run(
            "chmod", "755",
            stage.resolve("usr/bin/mjdevc").absolutePath,
            stage.resolve("usr/bin/mjdev-session").absolutePath
        )
        debFile.parentFile.mkdirs()
        run(
            "dpkg-deb", "--build", "--root-owner-group",
            stage.absolutePath, debFile.absolutePath
        )
        println("deb created: $debFile")
    }
}

// portable app image archive next to the deb in release/
tasks.register("packageMjdevAppImage") {
    group = "mjdev"
    description = "Packs the desktop app image into release/ as a portable tar.gz."
    dependsOn(":composeApp:createDistributable")
    val appName = libs.versions.app.name.get()
    val version = libs.versions.app.pkg.version.get()
    val image = rootDir.resolve("packages/main/app/$appName")
    val out = rootDir.resolve("release/$appName-$version-linux-x64.tar.gz")
    doLast {
        fun run(vararg args: String) {
            val process = ProcessBuilder(*args).inheritIO().start()
            check(process.waitFor() == 0) { "command failed: ${args.joinToString(" ")}" }
        }
        out.parentFile.mkdirs()
        run(
            "tar", "czf", out.absolutePath,
            "-C", image.parentFile.absolutePath, appName
        )
        println("app image created: $out")
    }
}

// android apk copied next to the deb in release/
tasks.register("packageMjdevApk") {
    group = "mjdev"
    description = "Builds the android apk into release/."
    dependsOn(":composeApp:assembleRelease")
    val appName = libs.versions.app.name.get()
    val version = libs.versions.app.pkg.version.get()
    val apk = project(":composeApp").layout.buildDirectory
        .file("outputs/apk/release/composeApp-release.apk").get().asFile
    val out = rootDir.resolve("release/$appName-$version-android.apk")
    doLast {
        out.parentFile.mkdirs()
        apk.copyTo(out, overwrite = true)
        println("apk created: $out")
    }
}

// installs the whole desktop (shell, compositor, wayland session) from the
// freshly built deb, after this the mjdev session is selectable on gdm
tasks.register("installDesktop") {
    group = "mjdev"
    description = "Builds the deb and installs it (sudo), session ready on gdm."
    dependsOn("packageMjdevDeb")
    val appName = libs.versions.app.name.get()
    val version = libs.versions.app.pkg.version.get()
    val debFile = rootDir.resolve("release/$appName-$version.deb")
    doLast {
        // pkexec shows a graphical password dialog - sudo cannot prompt
        // inside the ide run console (it is not a terminal)
        val elevate = if (File("/usr/bin/pkexec").canExecute() &&
            !System.getenv("DISPLAY").isNullOrBlank()
        ) "pkexec" else "sudo"
        val process = ProcessBuilder(
            elevate, "apt-get", "install", "-y", "--reinstall", debFile.absolutePath
        ).inheritIO().start()
        check(process.waitFor() == 0) { "installation failed" }
        println("installed - the mjdev session is available on the login screen")
    }
}

// everything the release workflow publishes - deb + app image + apk
tasks.register("buildAll") {
    group = "mjdev"
    description = "Builds every distributable package into release/ (deb + app image + apk)."
    dependsOn("packageMjdevDeb", "packageMjdevAppImage", "packageMjdevApk")
}

//</editor-fold>------------------------------------------------------------------------------------

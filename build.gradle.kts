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

//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="mjdev packaging tasks">---------------------------------------------------------

// single deb carrying the desktop shell, the mjdevc compositor and the
// wayland session files - after `apt install` the session is ready on gdm
tasks.register("packageMjdevDeb") {
    group = "mjdev"
    description = "Builds one deb with the desktop, the compositor and the wayland session."
    dependsOn(":composeApp:createDistributable", ":compositor:linkMjdevcReleaseExecutableLinuxX64")
    doLast {
        val appName = libs.versions.app.name.get()
        val version = libs.versions.app.pkg.version.get()
        val appDescription = libs.versions.app.description.get()
        // catalog format is "email<Name>", debian control wants "Name <email>"
        val maintainer = Regex("(.+)<(.+)>").find(libs.versions.app.maintainer.get())
            ?.let { "${it.groupValues[2].trim()} <${it.groupValues[1].trim()}>" }
            ?: libs.versions.app.maintainer.get()
        val stage = layout.buildDirectory.dir("deb/$appName").get().asFile
        stage.deleteRecursively()

        copy {
            from(rootDir.resolve("packages/main/app/$appName"))
            into(stage.resolve("opt/$appName"))
        }
        copy {
            from(project(":compositor").layout.buildDirectory
                .file("bin/linuxX64/mjdevcReleaseExecutable/mjdevc.kexe"))
            into(stage.resolve("usr/bin"))
            rename { "mjdevc" }
        }
        copy {
            from(rootDir.resolve("session/mjdev-session"))
            into(stage.resolve("usr/bin"))
        }
        copy {
            from(rootDir.resolve("session/mjdev.desktop"))
            into(stage.resolve("usr/share/wayland-sessions"))
        }

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

        exec {
            commandLine("chmod", "755",
                stage.resolve("usr/bin/mjdevc").absolutePath,
                stage.resolve("usr/bin/mjdev-session").absolutePath)
        }
        val debFile = rootDir.resolve("release/$appName-$version.deb")
        debFile.parentFile.mkdirs()
        exec {
            commandLine("dpkg-deb", "--build", "--root-owner-group",
                stage.absolutePath, debFile.absolutePath)
        }
        println("deb created: $debFile")
    }
}

// portable app image archive next to the deb in release/
tasks.register("packageMjdevAppImage") {
    group = "mjdev"
    description = "Packs the desktop app image into release/ as a portable tar.gz."
    dependsOn(":composeApp:createDistributable")
    doLast {
        val appName = libs.versions.app.name.get()
        val version = libs.versions.app.pkg.version.get()
        val image = rootDir.resolve("packages/main/app/$appName")
        val out = rootDir.resolve("release/$appName-$version-linux-x64.tar.gz")
        out.parentFile.mkdirs()
        exec {
            commandLine("tar", "czf", out.absolutePath,
                "-C", image.parentFile.absolutePath, appName)
        }
        println("app image created: $out")
    }
}

// everything the release workflow publishes - currently deb + app image
tasks.register("buildAll") {
    group = "mjdev"
    description = "Builds every distributable package into release/ (deb + app image)."
    dependsOn("packageMjdevDeb", "packageMjdevAppImage")
}

//</editor-fold>------------------------------------------------------------------------------------

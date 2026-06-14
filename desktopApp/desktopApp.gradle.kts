import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(libs.versions.java.language.version.get().toInt())
}

dependencies {
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
}

// IDE run-config compatibility: the old KMP module exposed `desktopRun`; a plain JVM
// Compose Desktop module exposes `run`. Keep `desktopRun` working as an alias.
tasks.register("desktopRun") {
    group = "compose desktop"
    description = "Alias for :desktopApp:run (IDE run configuration compatibility)."
    dependsOn("run")
}

compose.desktop {
    application {
        jvmArgs("-Dprism.order=sw")
        mainClass = "org.mjdev.desktop.main.MainKt"
        // jpackage (createDistributable/packageDeb) needs a full JDK; the IDE's bundled JBR
        // (e.g. Android Studio's) ships without jpackage, so resolve a jpackage-capable JDK
        // explicitly. Override with JPACKAGE_HOME or JAVA_HOME; falls back to system JDKs.
        sequenceOf(
            System.getenv("JPACKAGE_HOME"),
            System.getenv("JAVA_HOME"),
            "/usr/lib/jvm/java-21-openjdk-amd64",
            "/usr/lib/jvm/java-17-openjdk-amd64",
        ).filterNotNull().firstOrNull { rootProject.file("$it/bin/jpackage").canExecute() }
            ?.let { javaHome = it }
        nativeDistributions {
            packageName = libs.versions.app.name.get()
            packageVersion = libs.versions.app.pkg.version.get()
            description = libs.versions.app.description.get()
            copyright = libs.versions.app.copyright.get()
            outputBaseDir.set(rootProject.rootDir.resolve("packages"))
            linux {
                appCategory = libs.versions.app.category.get()
                debMaintainer = libs.versions.app.maintainer.get()
                menuGroup = libs.versions.app.menu.group.get()
                vendor = libs.versions.app.vendor.get()
            }
            targetFormats(
                TargetFormat.Deb,
                TargetFormat.Rpm,
                TargetFormat.AppImage,
                TargetFormat.Exe, // Windows installer — only built on a Windows host
                TargetFormat.Dmg, // macOS installer — only built on a macOS host
            )
            buildTypes.release.proguard {
                configurationFiles.from("desktopApp.pro")
                // optional transitive deps (netty -> log4j2) defeat the optimizer's class
                // hierarchy analysis (IncompleteClassHierarchyException); shrink+obfuscate stay on
                optimize.set(false)
            }
        }
    }
}

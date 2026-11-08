import org.jetbrains.compose.desktop.application.dsl.TargetFormat

// psippr login screen: a standalone greetd greeter app (replaces gdm), packaged as its own deb
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(
        libs.versions.java.language.version
            .get()
            .toInt(),
    )
}

dependencies {
    // login screen UI (Greeter, user row, session switch) and the greetd / PAM client
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        jvmArgs("-Dprism.order=sw")
        mainClass = "org.psippr.greeter.main.MainKt"
        // jpackage needs a full JDK, same lookup as :desktopApp (JPACKAGE_HOME, JAVA_HOME, system JDKs)
        sequenceOf(
            System.getenv("JPACKAGE_HOME"),
            System.getenv("JAVA_HOME"),
            "/usr/lib/jvm/java-21-openjdk-amd64",
            "/usr/lib/jvm/java-17-openjdk-amd64",
        ).filterNotNull()
            .firstOrNull { rootProject.file("$it/bin/jpackage").canExecute() }
            ?.let { javaHome = it }
        nativeDistributions {
            packageName =
                libs.versions.app.greeter.name
                    .get()
            packageVersion =
                libs.versions.app.pkg.version
                    .get()
            description =
                libs.versions.app.greeter.description
                    .get()
            copyright =
                libs.versions.app.copyright
                    .get()
            // own output dir, so the desktop's packages/main-release dirs only hold desktop packages
            outputBaseDir.set(rootProject.rootDir.resolve("packages/greeter"))
            linux {
                appCategory =
                    libs.versions.app.category
                        .get()
                debMaintainer =
                    libs.versions.app.maintainer
                        .get()
                vendor =
                    libs.versions.app.vendor
                        .get()
            }
            // a display manager is linux only and installed through apt
            targetFormats(TargetFormat.Deb)
            buildTypes.release.proguard {
                // same as :desktopApp - shrinking drops classes reached only via META-INF/services
                isEnabled.set(false)
            }
        }
    }
}

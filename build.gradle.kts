@file:Suppress("UnstableApiUsage")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
//import org.jetbrains.kotlin.config.JvmTarget
import org.mjdev.gradle.extensions.ProjectExt.createTask
import org.mjdev.gradle.extensions.ProjectExt.resolve

plugins {
//    java
//    application

//    kotlin("multiplatform") version "2.0.20"
//    alias(libs.plugins.jetbrainsCompose) apply false
//    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.gradle.versions)
//    DesktopPlugin
//    alias(libs.plugins.kotlin.kapt)
//    id("dev.datlag.sekret") version "2.0.0-alpha-07"
//    id("com.bintray.gradle.plugins.bintray")
}

group = libs.versions.packageName.get()
version = libs.versions.packageVersion.get()

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

kotlin {
//    jvm()
//    linuxX64() {
    /* Specify additional settings for the 'linux' target here */
//    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    jvmToolchain(21)
}

configurations {
    "implementation" {
        exclude(group = "androidx.compose.animation")
        exclude(group = "androidx.compose.foundation")
        exclude(group = "androidx.compose.material")
        exclude(group = "androidx.compose.runtime")
        exclude(group = "androidx.compose.ui")
    }
}

allprojects {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://androidx.dev/storage/compose-compiler/repository")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jogamp.org/deployment/maven")
        maven("https://jitpack.io")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        api(fileTree("src/main/libs") { include("*.jar") })
        // reflection
        implementation(kotlin("reflect"))
        // coroutines
        runtimeOnly(libs.kotlinx.coroutines.swing)
        // compose desktop
        implementation(compose.desktop.currentOs)
        // preview
        implementation(compose.runtime)
        // material 3
//        implementation(compose.material3)
        // icons
        implementation(compose.materialIconsExtended)
        // foundation
//        implementation(compose.foundation)
        // preview
//        implementation(compose.uiTooling)
//        implementation(compose.preview)
        // resources
//        implementation(compose.components.resources)
        // init & desktop files
        implementation(libs.ini4j)
        // http client
        implementation(libs.okhttp.client)
        // coil
        implementation(libs.coil.compose.core)
        implementation(libs.coil.compose)
        implementation(libs.coil.mp)
        implementation(libs.coil.svg)
        implementation(libs.coil.network.okhttp)
        // ffmpeg
        implementation(libs.ffmpeg.platform)
        // json
        implementation(libs.google.gson)
        // no log
        implementation(libs.slf4j.nop)
        // fuzzy search
        implementation(libs.fuzzywuzzy)
        // graphs
        implementation(libs.charts)
        // gemini ai
        implementation(libs.google.generativeai)
        // jna
        implementation(libs.jna.platform)
        // qr code
        implementation(libs.qrose)
        // tts
        implementation(libs.tts)
        // webview
        implementation(libs.compose.webview)
        // javascript
        implementation(libs.mozilla.rhino)
        // dbus
        implementation(libs.dbus.java.core)
        implementation(libs.dbus.java.transport.native.unixsocket)
        // testing yet :
        // gettext
        implementation("org.gnu.gettext:libintl:0.18.3")
        // custom components
        implementation("com.composables:core:1.12.0")
        // events
        implementation("org.rationalityfrontline:kevent:2.3.1")
        // components
        implementation("com.composables:core:1.17.1")
        // wayland
//        implementation("org.freedesktop:wayland-client:1.5.1")
        // paths
//        implementation("me.sujanpoudel.multiplatform.utils:multiplatform-paths:0.2.2")
        // files
//        implementation("io.github.vinceglb:filekit-core:0.8.2")
        // files with composable utilities
//        implementation("io.github.vinceglb:filekit-compose:0.8.2")
//        // images metadata
//        implementation("com.ashampoo:kim:0.18.4")
        // bottom sheet flexible
//        implementation("com.github.skydoves:flexible-bottomsheet-material3:0.1.5")
        // stt
//        implementation("com.alphacephei:vosk:0.3.32+")
        // fs watcher
//        implementation("io.github.irgaly.kfswatch:kfswatch:1.3.0")
        // lib-app-indicator
//        implementation("org.purejava:libappindicator-gtk3-java-full:1.4.1")
        // internationalization
//        implementation("org.swiftshire:ji18n-core:1.0")
        // sikulix
//        implementation("com.sikulix:sikulixapi:2.0.5")
        // blur
//        implementation("dev.chrisbanes.haze:haze:0.7.3")
        // java gnome
//        implementation("com.github.bailuk:java-gtk:0.5.0")
        // wayland
//        implementation("org.freedesktop:wayland:1.4.0")
        // wnck
        // todo
        // compose debugger
//        implementation("io.github.theapache64:rebugger:1.0.0-rc03")
        // music
//        implementation(libs.korau)
        // dbus
//        implementation("org.freedesktop.dbus:dbus-java:2.7")
//        implementation("org.freedesktop.dbus:dbus-java-annotations:2.7")
        // bar code
//        implementation("io.github.alexzhirkevich:qrose-oned:1.0.1")
        // flow extensions
//        implementation("io.github.hoc081098:FlowExt:1.0.0-RC")
        // metadata
//        implementation("com.ashampoo:kim:0.18.4")
        // html
//        implementation("com.github.Hamamas:Kotlin-Wasm-Html-Interop:1.0.1")
        // sockets
//        implementation("com.kohlschutter.junixsocket:junixsocket-core:2.3.2")
        // x11 client
//        implementation("com.github.moaxcp.x11:x11-client:0.18.2")
        // jwm
//        implementation("io.github.humbleui:jwm:0.4.8")
        // hid
//        implementation('org.hid4java:hid4java')
    }
}

compose {
    desktop {
        application {
            jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")
            if (System.getProperty("os.name").contains("Mac")) {
                jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
                jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
            }
            mainClass = "eu.mjdev.desktop.MainKt"
            nativeDistributions {
                packageName = libs.versions.appName.get()
                packageVersion = libs.versions.packageVersion.get()
                description = libs.versions.appDescription.get()
                copyright = libs.versions.appCopyright.get()
                modules("jdk.unsupported")
                outputBaseDir.set(project.rootDir.resolve("packages"))
//                macOS {
//                    iconFile.set(project.rootDir.resolve("icons").resolve("icon.icns"))
//                }
//                windows {
//                    iconFile.set(project.rootDir.resolve("icons").resolve("icon.ico"))
//                    exePackageVersion = "1.0.0"
//                    msiPackageVersion = "1.0.0"
//                }
                linux {
//                    iconFile.set(project.rootDir.resolve("icons").resolve("icon.png"))
//                    licenseFile.set(project.file("LICENSE.txt"))
                    appCategory = libs.versions.appCategory.get()
                    debMaintainer = libs.versions.appMaintainer.get()
                    menuGroup = libs.versions.appMenuGroup.get()
                    vendor = libs.versions.appVendor.get()
                    modules("jdk.security.auth")
                }
                targetFormats(
//                    TargetFormat.Dmg,
//                    TargetFormat.Msi,
                    TargetFormat.Deb,
//                    TargetFormat.Exe,
                    TargetFormat.AppImage,
//                    TargetFormat.Pkg,
                    TargetFormat.Rpm
                )
                buildTypes.release.proguard {
                    configurationFiles.from("compose-desktop.pro")
                }
//                appResourcesRootDir.set(project.rootDir.resolve("resources"))
            }
        }
    }
}

createTask(
    name = "cleanUp",
    group = "mjdev",
    description = "Task cleans project."
) {
    resolve(".gradle").deleteRecursively()
    resolve("build").deleteRecursively()
    resolve("buildSrc").resolve("build").deleteRecursively()
    resolve("buildSrc").resolve(".gradle").deleteRecursively()
    resolve("packages").deleteRecursively()
}

createTask(
    name = "createPackages",
    group = "mjdev",
    description = "Task packages project to be published."
) {
    dependsOn("packageAppImage")
    dependsOn("packageDeb")
//    dependsOn("packageDmg")
//    dependsOn("packageExe")
//    dependsOn("packageMsi")
//    dependsOn("packagePkg")
//    dependsOn("packageRpm")
//    dependsOn("packageReleaseDeb")
}

createTask<JavaExec>(
    name = "runDesktop",
    group = "mjdev",
    description = "Run app"
) {
    mainClass = "eu.mjdev.desktop.MainKt"
    classpath = sourceSets.main.get().runtimeClasspath
    jvmArgs("--enable-preview")
}

createTask<JavaExec>(
    name = "runDebug",
    group = "mjdev",
    description = "Run app with debug parameter."
) {
    mainClass = "eu.mjdev.desktop.MainKt"
    classpath = sourceSets.main.get().runtimeClasspath
    args = arrayListOf("--debug")
    jvmArgs("--enable-preview")
    // dbus-run-session mutter --wayland --nested
}

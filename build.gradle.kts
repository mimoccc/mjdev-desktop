import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.mjdev.gradle.extensions.ProjectExt.createTask
import org.mjdev.gradle.extensions.ProjectExt.resolve

plugins {
    java
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.gradle.versions)
//    DesktopPlugin
//    alias(libs.plugins.kotlin.kapt)
//    id("dev.datlag.sekret") version "2.0.0-alpha-07"
//    id("com.bintray.gradle.plugins.bintray")
}

group = libs.versions.packageName.get()
version = libs.versions.packageVersion.get()

java {
    sourceCompatibility = JavaVersion.VERSION_22
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

kotlin {

}

application {
    mainClass = "eu.mjdev.desktop.MainKt"
}

//configurations {
//    "implementation" {
//        exclude(group = "androidx.compose.animation")
//        exclude(group = "androidx.compose.foundation")
//        exclude(group = "androidx.compose.material")
//        exclude(group = "androidx.compose.runtime")
//        exclude(group = "androidx.compose.ui")
//    }
//}

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
//        implementation(kotlin("reflect"))
        // coroutines
        runtimeOnly(libs.kotlinx.coroutines.swing)
        // compose desktop
//        implementation(compose.desktop.currentOs)
        // preview
//        implementation(compose.runtime)
        // material 3
//        implementation(compose.material3)
        // icons
//        implementation(compose.materialIconsExtended)
        // foundation
//        implementation(compose.foundation)
        // preview
//        implementation(compose.uiTooling)
//        implementation(compose.preview)
        // resources
//        implementation(compose.components.resources)
        // gobject
        implementation("io.github.jwharm.javagi:gobject:0.10.2")
        // glib
        implementation("io.github.jwharm.javagi:glib:0.10.2")
        // gmodule
        implementation("io.github.jwharm.javagi:gmodule:0.10.2")
        // gio
        implementation("io.github.jwharm.javagi:gio:0.10.2")
        // gst
        implementation("io.github.jwharm.javagi:gst:0.10.2")
        // gsk
        implementation("io.github.jwharm.javagi:gsk:0.10.2")
        // gdk
        implementation("io.github.jwharm.javagi:gdk:0.10.2")
        // gtk
        implementation("io.github.jwharm.javagi:gtk:0.10.2")
        // pango
        implementation("io.github.jwharm.javagi:pango:0.10.2")
        // pangocairo
        implementation("io.github.jwharm.javagi:pangocairo:0.10.2")
        // javascript core
        implementation("io.github.jwharm.javagi:javascriptcore:0.10.2")
        // gdkpixbuf
        implementation("io.github.jwharm.javagi:gdkpixbuf:0.10.2")
        // gstaudio
        implementation("io.github.jwharm.javagi:gstaudio:0.10.2")
        // graphene
        implementation("io.github.jwharm.javagi:graphene:0.10.2")
        // gstvideo
        implementation("io.github.jwharm.javagi:gstvideo:0.10.2")
        // webkitwebprocessextension
        implementation("io.github.jwharm.javagi:webkitwebprocessextension:0.10.2")
        // gtksourceview
        implementation("io.github.jwharm.javagi:gtksourceview:0.10.2")
        // adv
        implementation("io.github.jwharm.javagi:adw:0.10.2")
        // cairo
//        implementation ("io.github.jwharm.cairobindings:cairo:1.18.4")
        // compose gtk
//        implementation("io.github.mmarco94:compose-4-gtk:0.1")
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
        // dbus
//        implementation(libs.dbus.java.core)
//        implementation(libs.dbus.java.transport.native.unixsocket)
        // wayland
//        implementation("org.freedesktop:wayland-client:1.5.1")
        // gettext
        implementation("org.gnu.gettext:libintl:0.18.3")
        // constraint
//        implementation("tech.annexflow.compose:constraintlayout-compose-multiplatform:0.4.0")
//        implementation("tech.annexflow.compose:constraintlayout-compose-multiplatform:0.5.0-alpha03")
//        implementation("tech.annexflow.compose:constraintlayout-compose-multiplatform:0.5.0-alpha03-shaded-core")
//        implementation("tech.annexflow.compose:constraintlayout-compose-multiplatform:0.5.0-alpha03-shaded")
        // paths
//        implementation("me.sujanpoudel.multiplatform.utils:multiplatform-paths:0.2.2")
        // files
//        implementation("io.github.vinceglb:filekit-core:0.8.2")
        // files with composable utilities
//        implementation("io.github.vinceglb:filekit-compose:0.8.2")
        // testing yet
        // anim
//        implementation("org.jetbrains.compose.animation:animation:1.7.0-alpha03")
//        implementation("org.jetbrains.compose.animation:animation-graphics:1.7.0-alpha03")
//        // images metadata
//        implementation("com.ashampoo:kim:0.18.4")
        // connectivity
        implementation("dev.tmapps:konnection:1.4.1")
        // web
        implementation("io.github.kevinnzou:compose-webview-multiplatform:1.9.40-alpha01")
        // bottom sheet flexible
//        implementation("com.github.skydoves:flexible-bottomsheet-material3:0.1.5")
        // stt
//        implementation("com.alphacephei:vosk:0.3.32+")
        // fs watcher
//        implementation("io.github.irgaly.kfswatch:kfswatch:1.3.0")
        // custom components
        implementation("com.composables:core:1.12.0")
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
        // javascript
        // implementation(libs.mozilla.rhino)
//        implementation ("androidx.compose.ui:ui-text-google-fonts:1.6.8")
    }
}

//tasks.withType<JavaExec>().configureEach {
//    jvmArgs(
//        "--enable-preview",
//        "--enable-native-access=ALL-UNNAMED",
//        "-Djava.library.path=/usr/lib64:/lib64:/lib:/usr/lib:/lib/x86_64-linux-gnu"
//    )
//}

//tasks.withType<Test>().configureEach {
//    useJUnitPlatform()
//    jvmArgs("--enable-preview")
//}

//tasks.withType<Copy>() {
//    from {
//        fileTree("native") {
//            include ("*.so", "*.dylib", "*.dll")
//        }
//    }
//    into {
//        configurations.runtimeClasspath.files.collect { it.absolutePath }
//    }
//}

//bintray {
//    user = 'your_bintray_username'
//    key = 'your_bintray_api_key'
//
//    publish = true
//    pkg {
//        repo = 'your_repository_name'
//        name = project.name
//        desc = 'Your desktop Compose application'
//        version = project.version
//        licenses = ['Apache-2.0']
//        vcsUrl = 'https://github.com/your_username/your_project'
//
//        // Configure deb package dependencies
//        deb {
//            packageDescription = 'Your desktop Compose application'
//            packageVersion = project.version
//            packageArchitecture = 'amd64'  // Adjust based on your target architecture
//            packageDepends = ['dbus-java', 'libgtk-3-dev']  // Replace with your actual dependencies
//        }
//    }
//}

//compose {
//    desktop {
//        application {
//            jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
//            jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")
//            if (System.getProperty("os.name").contains("Mac")) {
//                jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
//                jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
//            }
//            mainClass = "eu.mjdev.desktop.MainKt"
//            nativeDistributions {
//                packageName = libs.versions.appName.get()
//                packageVersion = libs.versions.packageVersion.get()
//                description = libs.versions.appDescription.get()
//                copyright = libs.versions.appCopyright.get()
//                modules("jdk.unsupported")
//                outputBaseDir.set(project.rootDir.resolve("packages"))
////                macOS {
////                    iconFile.set(project.rootDir.resolve("icons").resolve("icon.icns"))
////                }
////                windows {
////                    iconFile.set(project.rootDir.resolve("icons").resolve("icon.ico"))
////                    exePackageVersion = "1.0.0"
////                    msiPackageVersion = "1.0.0"
////                }
//                linux {
////                    iconFile.set(project.rootDir.resolve("icons").resolve("icon.png"))
////                    licenseFile.set(project.file("LICENSE.txt"))
//                    appCategory = libs.versions.appCategory.get()
//                    debMaintainer = libs.versions.appMaintainer.get()
//                    menuGroup = libs.versions.appMenuGroup.get()
//                    vendor = libs.versions.appVendor.get()
//                    modules("jdk.security.auth")
//                }
//                targetFormats(
////                    TargetFormat.Dmg,
////                    TargetFormat.Msi,
//                    TargetFormat.Deb,
////                    TargetFormat.Exe,
//                    TargetFormat.AppImage,
////                    TargetFormat.Pkg,
//                    TargetFormat.Rpm
//                )
//                buildTypes.release.proguard {
//                    configurationFiles.from("compose-desktop.pro")
//                }
////                appResourcesRootDir.set(project.rootDir.resolve("resources"))
//            }
//        }
//    }
//}

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
//    dependsOn("packageAppImage")
//    dependsOn("packageDeb")
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
    jvmArgs(
        "--enable-preview",
        "--enable-native-access=ALL-UNNAMED",
        "-Djava.library.path=/usr/lib64:/lib64:/lib:/usr/lib:/lib/x86_64-linux-gnu"
    )
}
createTask<JavaExec>(
    name = "runDebug",
    group = "mjdev",
    description = "Run app with debug parameter."
) {
    mainClass = "eu.mjdev.desktop.MainKt"
    classpath = sourceSets.main.get().runtimeClasspath
    args = arrayListOf("--debug")
    jvmArgs(
        "--enable-preview",
        "--enable-native-access=ALL-UNNAMED",
        "-Djava.library.path=/usr/lib64:/lib64:/lib:/usr/lib:/lib/x86_64-linux-gnu"
    )
    // dbus-run-session mutter --wayland --nested
}

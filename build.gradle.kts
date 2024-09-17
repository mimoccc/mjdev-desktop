import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.kotlin.serialization)
//    id("dev.datlag.sekret") version "2.0.0-alpha-07"
}

group = libs.versions.packageName.get()
version = libs.versions.packageVersion.get()

allprojects {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://androidx.dev/storage/compose-compiler/repository")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
        maven("https://jogamp.org/deployment/maven")
        maven("https://jitpack.io")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        // reflection
//        implementation(kotlin("reflect"))
        // coroutines
        runtimeOnly(libs.kotlinx.coroutines.swing)
        // compose desktop
        implementation(compose.desktop.currentOs)
        // material 3
        implementation(compose.material3)
        // icons
        implementation(compose.materialIconsExtended)
        // foundation
        implementation(compose.foundation)
        // preview
        implementation(compose.preview)
        // resources
        implementation(compose.components.resources)
        // init & desktop files
        implementation(libs.ini4j)
        // http client
        implementation(libs.okhttp.client)
        // coil
        implementation(libs.coil.compose.core)
        implementation(libs.coil.compose)
        implementation(libs.coil.mp)
        implementation(libs.coil.svg)
//        implementation(libs.coil.gif)
        implementation(libs.coil.network.okhttp)
        // ffmpeg
        implementation(libs.ffmpeg.platform)
        // music
//        implementation(libs.korau)
        // palette
        implementation(libs.material.kolor)
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
        // compose debugger
        implementation("io.github.theapache64:rebugger:1.0.0-rc03")
        // qr code
        implementation("io.github.alexzhirkevich:qrose:1.0.1")
        // connectivity
        implementation("dev.tmapps:konnection:1.4.1")
        // web
        implementation("io.github.kevinnzou:compose-webview-multiplatform:1.9.40-alpha01")
        // bottom sheet flexible
        implementation("com.github.skydoves:flexible-bottomsheet-material3:0.1.5")
        // fs watcher
        implementation("io.github.irgaly.kfswatch:kfswatch:1.3.0")
        // tts
        implementation("nl.marc-apps:tts:2.5.0")
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
        // jna
        implementation("net.java.dev.jna:jna-platform:5.6.0")
        // hid
//        implementation('org.hid4java:hid4java')
        // dbus
        // implementation(libs.dbus.java.core)
        // implementation(libs.dbus.java.transport.native.unixsocket)
        // javascript
        // implementation(libs.mozilla.rhino)

//        implementation("com.github.SmartToolFactory:Compose-Extended-Colors:1.0.0-alpha06")
//        implementation("com.github.SmartToolFactory:Compose-Extended-Gestures:2.0.0")
//        implementation ("androidx.compose.ui:ui-text-google-fonts:1.6.8")
    }
}

fun Project.createTask(
    name: String,
    group: String = "mjdev",
    description: String = "",
    configureAction: Task.() -> Unit
) = tasks.create(name).apply {
    this.group = group
    this.description = description
    configureAction.invoke(this)
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
                }
                targetFormats(
//                    TargetFormat.Dmg,
//                    TargetFormat.Msi,
                    TargetFormat.Deb,
//                    TargetFormat.Exe,
//                    TargetFormat.AppImage,
//                    TargetFormat.Pkg,
//                    TargetFormat.Rpm
                )
//                buildTypes.release.proguard {
//                    configurationFiles.from("compose-desktop.pro")
//                }
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
    project.rootDir.resolve(".gradle").deleteRecursively()
    project.rootDir.resolve("build").deleteRecursively()
    project.rootDir.resolve("buildSrc").resolve("build").deleteRecursively()
    project.rootDir.resolve("buildSrc").resolve(".gradle").deleteRecursively()
    project.rootDir.resolve("packages").deleteRecursively()
}

createTask(
    name = "createPackages",
    group = "mjdev",
    description = "Task packages project to be published."
) {
    dependsOn("packageDeb")
}

createTask(
    name = "runDesktop",
    group = "mjdev",
    description = "Task packages project to be published."
) {
    dependsOn("run")
}

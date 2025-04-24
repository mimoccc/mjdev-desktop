@file:Suppress("OPT_IN_USAGE")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
//    MultiPlatformPlugin
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvm("desktop")
    wasmJs {
        outputModuleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    sourceSets {
        // common dependencies
        commonMain {
            dependencies {
                // reflection
                implementation(kotlin("reflect"))
                // preview
                implementation(compose.components.uiToolingPreview)
                // base
                implementation(projects.shared)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.animation)
                implementation(compose.animationGraphics)
                implementation(compose.components.resources)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                // date time
                implementation(libs.kotlinx.datetime)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                // images
                implementation(libs.coil.compose)
                implementation(libs.coil.svg)
                implementation(libs.coil.mp)
                // charts
                implementation(libs.charts)
                // qr code
                implementation(libs.qrose)
                // json
                implementation(libs.google.gson)
                // desktop file linux todo move to desktop
                implementation(libs.ini4j)
                // tts
                implementation(libs.tts)
                // flowmvi
//                implementation(libs.flowmvi.core)
//                implementation(libs.flowmvi.compose)
//                implementation(libs.flowmvi.android)
//                implementation(libs.flowmvi.savedstate)
//                implementation(libs.flowmvi.debugger)
                // locale
//            implementation(libs.i18n4k)
                // mo po gettext
                implementation(libs.gettext.lib)
                // timeline
                implementation(libs.jetlime)
                // anims kottie
//            implementation("io.github.ismai117:kottie:2.0.1")
                // kbd
//            implementation("com.github.animeshz:keyboard-kt-jvm:0.3.3")
                // mouse
//            implementation("com.github.animeshz:mouse-kt-jvm:0.3.3")
                // rss
//            implementation("com.prof18.rssparser:rssparser:6.0.8")
                // korlibs
//            implementation("com.soywiz:korlibs-io:6.0.0")
//            implementation("com.soywiz:korlibs-io-fs:6.0.0")
                // rich text editor
//            implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc10")
//                implementation (libs.popkorn)
//                kapt(libs.popkorn.compiler)
//                implementation("io.github.khubaibkhan4:mediaplayer-kmp:2.0.6")
                // strings
                implementation(libs.human.readable)
                // alerts
                implementation(libs.alert.kmp)
                // tor
                // implementation("io.matthewnelson.kmp-tor:runtime:2.1.0")
                // files
                implementation("com.squareup.okio:okio:3.10.2")
                // ai
//                implementation(libs.generativeai)
            }
        }
        // android dependencies
        androidMain {
            dependencies {
                // reflection
                implementation(kotlin("reflect"))
                // preview
                implementation(compose.components.uiToolingPreview)
                // activity
                implementation(libs.androidx.activity.compose)
                // network client
                implementation(libs.okhttp3.client)
                // images
                implementation(libs.coil.okhttp)
                // splash
                implementation(libs.androidx.core.splashscreen)
                // permissions
                implementation(libs.accompanist.permissions)
                // ai gemini
                implementation(libs.sh.google.generative.ai)
            }
        }
        // wasm dependencies
        wasmJsMain {
            dependencies {
                // reflection
                implementation(kotlin("reflect"))
                // preview
                implementation(compose.components.uiToolingPreview)
                // ai gemini
                implementation(libs.sh.google.generative.ai)
            }
        }
        // desktop dependencies
        val desktopMain by getting
        desktopMain.dependencies {
            // reflection
            implementation(kotlin("reflect"))
            // preview
            implementation(compose.components.uiToolingPreview)
            // desktop
            implementation(compose.desktop.currentOs)
            // coroutines
            implementation(libs.kotlinx.coroutines.swing)
            // okhttp
            implementation(libs.okhttp3.client)
            // images okhttp
            implementation(libs.coil.okhttp)
            // ai gemini
            implementation(libs.sh.google.generative.ai)
            // chatgpt
            implementation(libs.ychat)
            // desktop files
            implementation(libs.ini4j)
        }
    }
}

// android app config
android {
    namespace = "org.mjdev.desktop"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/composeResources")
    sourceSets["main"].assets.srcDirs("src/commonMain/resources")
    defaultConfig {
        applicationId = "org.mjdev.desktop"
        minSdk = libs.versions.android.min.sdk.get().toInt()
        versionCode = libs.versions.app.pkg.version.get().replace(".", "").toInt()
        versionName = libs.versions.app.pkg.version.get()
        resValue("string", "app_name", libs.versions.app.name.get())
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    dependencies {
        debugImplementation(libs.androidx.ui.tooling)
    }
}

// remove
//dependencies {
//    implementation(libs.generativeai)
//}

// desktop app config
compose {
    desktop {
        group = libs.versions.app.pkg.name.get()
        version = libs.versions.app.pkg.version.get()
        application {
            mainClass = "org.mjdev.desktop.main.MainKt"
            nativeDistributions {
                packageName = libs.versions.app.name.get()
                packageVersion = libs.versions.app.pkg.version.get()
                description = libs.versions.app.description.get()
                copyright = libs.versions.app.copyright.get()
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
//                    iconFile.set(
//                        project.rootDir
//                            .resolve("composeApp")
//                            .resolve("src")
//                            .resolve("commonMain")
//                            .resolve("resources")
//                            .resolve("drawable")
//                            .resolve("icon.png")
//                    )
//                    licenseFile.set(project.rootDir.resolve("LICENSE.txt"))
                    appCategory = libs.versions.app.category.get()
                    debMaintainer = libs.versions.app.maintainer.get()
                    menuGroup = libs.versions.app.menu.group.get()
                    vendor = libs.versions.app.vendor.get()
                }
                targetFormats(
                    TargetFormat.Dmg,
                    TargetFormat.Msi,
                    TargetFormat.Deb,
                    TargetFormat.Exe,
                    TargetFormat.AppImage,
                    TargetFormat.Pkg,
                    TargetFormat.Rpm
                )
                buildTypes.release.proguard {
                    configurationFiles.from("compose-desktop.pro")
                }
//                appResourcesRootDir.set(project.rootDir.resolve("resources"))
            }
        }
    }
    resources {
        publicResClass = true
        packageOfResClass = "org.mjdev.desktop.resources"
        generateResClass = auto
    }
}

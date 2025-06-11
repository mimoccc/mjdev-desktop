/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

//<editor-fold desc="imports">----------------------------------------------------------------------

import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithWasmPresetFunctions
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="plugins">----------------------------------------------------------------------

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vlc.setup)
}

//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="helpers">----------------------------------------------------------------------

fun NamedDomainObjectContainer<KotlinSourceSet>.getOrCreate(
    name: String
): KotlinSourceSet = findByName(name) ?: create(name)

fun NamedDomainObjectContainer<KotlinSourceSet>.desktopMain(
    block: KotlinSourceSet.() -> Unit = {}
): KotlinSourceSet = getOrCreate("desktopMain").apply(block)

fun KotlinTargetContainerWithPresetFunctions.desktopTarget(
    configure: Action<KotlinJvmTarget>
) = jvm("desktop").apply {
    configure.execute(this)
}

@OptIn(ExperimentalWasmDsl::class)
fun KotlinTargetContainerWithWasmPresetFunctions.wasmJsTarget(
    configure: Action<KotlinWasmJsTargetDsl>
) = wasmJs(configure)

val Project.compose: ComposeExtension
    get() = extensions.getByType()

fun Project.desktop(
    config: DesktopExtension.() -> Unit
) = compose.desktop(config)

fun Project.composeResources(
    config: ResourcesExtension.() -> Unit
) = compose.resources(config)

fun targets(
    config: Action<KotlinMultiplatformExtension>
) = kotlin(config)

// todo
@Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
fun Project.wasmjs(config: DesktopExtension.() -> Unit) {
    // todo
//    compose.wasmJs(config)
}

// todo
fun Project.kotlinSourceSets(
    config: Action<NamedDomainObjectContainer<KotlinSourceSet>>
) = with(kotlin) {
    sourceSets(config)
}

//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="configurations">---------------------------------------------------------------

// java version
setJavaLanguageVersion(libs.versions.java.language.version)

// kotlin toolchain java version
kotlin {
    jvmToolchain(libs.versions.java.language.version.get().toInt())
}

// resources config
composeResources {
    publicResClass = true
    packageOfResClass = "org.mjdev.desktop.resources"
    generateResClass = always
}

// setup vlc plugin configuration
vlcSetup {
    val appVlcDir: String = libs.versions.vlc.dir.get()
    val appVlcVersion: String = libs.versions.vlc.version.get()
    vlcVersion.set(appVlcVersion)
    shouldCompressVlcFiles = true
    shouldIncludeAllVlcFiles = false
    pathToCopyVlcLinuxFilesTo = rootDir.resolve(appVlcDir)
//        pathToCopyVlcMacosFilesTo = rootDir.resolve(appVlcDir)
//        pathToCopyVlcWindowsFilesTo = rootDir.resolve(appVlcDir)
}

//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="targets">----------------------------------------------------------------------

targets {
    desktopTarget {
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

}

//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="source sets / dependencies">---------------------------------------------------

kotlin {
    sourceSets {

        // common dependencies
        commonMain {
            dependencies {
                // reflection
                implementation(kotlin("reflect"))
                // preview
                implementation(compose.components.uiToolingPreview)
                // base
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.animation)
                implementation(compose.animationGraphics)
                implementation(compose.components.resources)
                // lifecycles
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                // date time
                implementation(libs.kotlinx.datetime)
                // images
                implementation(libs.coil.compose)
                implementation(libs.coil.svg)
                implementation(libs.coil.mp)
                // charts
                implementation(libs.charts)
                // qr code
                implementation(libs.qrose)
                // json
                implementation(libs.kotlinx.serialization.json)
                // desktop file linux
                implementation(libs.ini4j)
                // tts
                implementation(libs.tts)
                implementation(libs.tts.compose)
                // blur
                implementation(libs.haze)
                // vlc
                implementation(libs.vlcj)
                // mo po gettext
                implementation(libs.gettext.lib)
                // timeline
                implementation(libs.jetlime)
                // notification
                // implementation("io.github.shadmanadman:knotif:0.56.0")
                // sensors
                //implementation("io.github.shadmanadman:KSensor:0.59.0")
                // flowmvi
//                implementation(libs.flowmvi.core)
//                implementation(libs.flowmvi.compose)
//                implementation(libs.flowmvi.android)
//                implementation(libs.flowmvi.savedstate)
//                implementation(libs.flowmvi.debugger)
                // locale
//            implementation(libs.i18n4k)
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
                implementation(libs.okio)
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
                // sensors
                //implementation("io.github.shadmanadman:KSensor:0.59.0")
            }
        }

        // desktop dependencies
        desktopMain {
            dependencies {
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
}

//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="platforms">--------------------------------------------------------------------

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

// desktop app config
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
//                TargetFormat.Dmg,
//                TargetFormat.Msi,
                TargetFormat.Deb,
//                TargetFormat.Exe,
                TargetFormat.AppImage,
//                TargetFormat.Pkg,
                TargetFormat.Rpm
            )
            buildTypes.release.proguard {
                configurationFiles.from("composeApp.pro")
            }
//            appResourcesRootDir.set(project.rootDir.resolve("resources"))
        }
    }
}

//</editor-fold>------------------------------------------------------------------------------------

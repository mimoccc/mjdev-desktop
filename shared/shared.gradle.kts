/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

// <editor-fold desc="imports">----------------------------------------------------------------------

import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

// </editor-fold>------------------------------------------------------------------------------------

// <editor-fold desc="plugins">----------------------------------------------------------------------

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vlc.setup)
//    alias(libs.plugins.devtools.ksp)
}

// </editor-fold>------------------------------------------------------------------------------------

// <editor-fold desc="helpers">----------------------------------------------------------------------

fun NamedDomainObjectContainer<KotlinSourceSet>.getOrCreate(name: String): KotlinSourceSet = findByName(name) ?: create(name)

fun NamedDomainObjectContainer<KotlinSourceSet>.desktopMain(block: KotlinSourceSet.() -> Unit = {}): KotlinSourceSet =
    getOrCreate("desktopMain").apply(block)

fun KotlinTargetContainerWithPresetFunctions.desktopTarget(configure: Action<KotlinJvmTarget>) =
    jvm("desktop").apply {
        configure.execute(this)
    }

val Project.compose: ComposeExtension
    get() = extensions.getByType()

fun Project.desktop(config: DesktopExtension.() -> Unit) = compose.desktop(config)

fun Project.composeResources(config: ResourcesExtension.() -> Unit) = compose.resources(config)

fun targets(config: Action<KotlinMultiplatformExtension>) = kotlin(config)

// </editor-fold>------------------------------------------------------------------------------------

// <editor-fold desc="configurations">---------------------------------------------------------------

// java version
setJavaLanguageVersion(libs.versions.java.language.version)

// kotlin toolchain java version
kotlin {
    jvmToolchain(
        libs.versions.java.language.version
            .get()
            .toInt(),
    )
}

// resources config
composeResources {
    publicResClass = true
    packageOfResClass = "org.mjdev.desktop.resources"
    generateResClass = always
}

// setup vlc plugin configuration
vlcSetup {
    val appVlcDir: String =
        libs.versions.vlc.dir
            .get()
    val appVlcVersion: String =
        libs.versions.vlc.version
            .get()
    vlcVersion.set(appVlcVersion)
    shouldCompressVlcFiles = true
    shouldIncludeAllVlcFiles = false
    pathToCopyVlcLinuxFilesTo = rootDir.resolve(appVlcDir)
//        pathToCopyVlcMacosFilesTo = rootDir.resolve(appVlcDir)
//        pathToCopyVlcWindowsFilesTo = rootDir.resolve(appVlcDir)
}

// </editor-fold>------------------------------------------------------------------------------------

// <editor-fold desc="targets">----------------------------------------------------------------------

targets {
    desktopTarget {
    }

    androidLibrary {
        namespace = "org.mjdev.desktop.shared"
        compileSdk =
            libs.versions.android.compile.sdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.min.sdk
                .get()
                .toInt()
        androidResources { enable = true }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

// </editor-fold>------------------------------------------------------------------------------------

// <editor-fold desc="source sets / dependencies">---------------------------------------------------

kotlin {
//    linuxX64 {
//        compilations.getByName("main") {
//            cinterops {
//                val waylandClient by creating {
//                    defFile(project.file("src/nativeInterop/cinterop/wayland.def"))
//                    packageName = "wayland.client"
//                }
//            }
//        }
//    }

//    linuxX64 {
//        binaries {
//            executable {
//                entryPoint = "main"
//            }
//        }
//    }

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
                implementation(compose.components.uiToolingPreview)
                // lifecycles
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                // log
                implementation(libs.napier)
                // crypto
                implementation(libs.cryptography.core)
                implementation(libs.cryptography.provider.optimal)
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
                implementation("com.google.code.gson:gson:2.10.1")
                // desktop file linux
                implementation(libs.ini4j)
                // files
                implementation(libs.okio)
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
                // html to text
                implementation(libs.html2text)
                // markdown
                implementation(libs.markwon)
                // strings
                implementation(libs.human.readable)
                // alerts
                implementation(libs.alert.kmp)
                // ai plugins
                implementation(libs.flock.aigentic.core)
                implementation(libs.flock.aigentic.openai)
                implementation(libs.flock.aigentic.gemini)
                implementation(libs.flock.aigentic.ollama)
                implementation(libs.flock.aigentic.vertexai)
                implementation(libs.flock.aigentic.http)
                implementation(libs.flock.aigentic.openapi)
                // ksp("community.flock.aigentic:ksp-processor:0.1.0")
                // ktor
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)
                // okhttp
                implementation(libs.okhttp3.client)
                implementation(libs.okhttp3.logging.interceptor)
                implementation(libs.okhttp3.urlconnection)
                implementation(libs.okhttp3.dnsoverhttps)
                implementation(libs.okhttp3.tls)
                implementation("dev.kotbase:couchbase-lite:3.1.9-1.1.1")
                implementation("io.ktor:ktor-server-core:3.2.1")
                implementation("io.ktor:ktor-server-netty:3.2.1")
                implementation("io.ktor:ktor-server-default-headers:3.2.1")
                implementation("io.ktor:ktor-server-call-logging:3.2.1")
                implementation("io.ktor:ktor-server-content-negotiation:3.2.1")
                implementation("io.ktor:ktor-serialization-gson:3.2.1")
                implementation("io.ktor:ktor-server-status-pages:3.2.1")
                implementation("io.ktor:ktor-server-cors:3.2.1")
                implementation("io.ktor:ktor-server-auth:3.2.1")
                implementation("io.ktor:ktor-server-compression:3.2.1")
                implementation("ch.qos.logback:logback-classic:1.3.11")
                // ai
                implementation("ai.koog:koog-agents:0.3.0")
                // implementation("com.squareup.okhttp3:okhttp-ws:4.12.0")
                // notification
                // implementation("io.github.shadmanadman:knotif:0.56.0")
                // sensors
                // implementation("io.github.shadmanadman:KSensor:0.59.0")
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
                // tor
                // implementation("io.matthewnelson.kmp-tor:runtime:2.1.0")
                // web
                implementation("io.github.shadmanadman:kwebview:1.31.0")
                // adb
                implementation("dev.mobile:dadb:1.2.10")
            }
        }

        // android dependencies
        androidMain {
            dependencies {
                // reflection
                implementation(kotlin("reflect"))
                // preview
//                implementation(compose.components.uiToolingPreview)
                // preview rendering in the IDE (org.jetbrains.compose.ui:ui-tooling)
                implementation(compose.uiTooling)
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
                // sensors
                // implementation("io.github.shadmanadman:KSensor:0.59.0")
                // adb
                // implementation("dev.mobile:dadb:1.2.10")
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
                // chatgpt
                implementation(libs.ychat)
                // desktop files
                implementation(libs.ini4j)
                // adb
//                implementation("dev.mobile:dadb:1.2.10")
                // wayland
//                implementation("org.freedesktop:wayland:1.4.0")
            }
        }

//        val linuxX64Main by getting {
//            dependencies {
//                // Linux-specific dependencies
//            }
//        }
    }
}

// </editor-fold>------------------------------------------------------------------------------------

// <editor-fold desc="platforms">--------------------------------------------------------------------

// android app config -> moved to :androidApp module
// desktop app config -> moved to :desktopApp module

// </editor-fold>------------------------------------------------------------------------------------

@file:Suppress("UnstableApiUsage")

//<editor-fold desc="root project">-----------------------------------------------------------------
rootProject.name = "mjdev-desktop"
//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="features">---------------------------------------------------------------------
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="plugin management">------------------------------------------------------------
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://androidx.dev/storage/compose-compiler/repository")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jogamp.org/deployment/maven")
        maven("https://jitpack.io")
        maven("https://maven.mozilla.org/maven2/")
        maven("https://maven.pkg.github.com/tuProlog/2p-kt")
        maven("https://dl.bintray.com/animeshz/maven")
        maven("https://plugins.gradle.org/m2/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://dl.bintray.com/kotlin/ktor")
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        google()
    }
}
//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="dependency management">--------------------------------------------------------
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://androidx.dev/storage/compose-compiler/repository")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jogamp.org/deployment/maven")
        maven("https://jitpack.io")
        maven("https://maven.mozilla.org/maven2/")
        maven("https://maven.pkg.github.com/tuProlog/2p-kt")
        maven("https://dl.bintray.com/animeshz/maven")
        maven("https://plugins.gradle.org/m2/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://dl.bintray.com/kotlin/ktor")
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        google()
    }
}
//</editor-fold>------------------------------------------------------------------------------------

//<editor-fold desc="includes">---------------------------------------------------------------------
include(":studio-plugin")
include(":composeApp")
include(":shared")
//</editor-fold>------------------------------------------------------------------------------------

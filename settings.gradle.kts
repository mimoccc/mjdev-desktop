@file:Suppress("UnstableApiUsage")

// <editor-fold desc="root project">-----------------------------------------------------------------

rootProject.name = "psippr-desktop"
// </editor-fold>------------------------------------------------------------------------------------

// <editor-fold desc="features">---------------------------------------------------------------------
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// </editor-fold>------------------------------------------------------------------------------------

// <editor-fold desc="plugin management">------------------------------------------------------------
pluginManagement {
    repositories {
        // google first: the Android Gradle Plugin and com.android.tools live only there
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://androidx.dev/storage/compose-compiler/repository")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        // old: unfiltered and before google(), so AGP + com.android.tools were looked up on jogamp.org
        // first - its timeouts broke the root project configuration of buildAll
//        maven("https://jogamp.org/deployment/maven")
        // jogamp only serves org.jogamp (gluegen / jogl), never ask it for anything else
        exclusiveContent {
            forRepository { maven("https://jogamp.org/deployment/maven") }
            filter { includeGroupByRegex("org\\.jogamp(\\..*)?") }
        }
        maven("https://jitpack.io")
        maven("https://maven.mozilla.org/maven2/")
        maven("https://maven.pkg.github.com/tuProlog/2p-kt")
        maven("https://dl.bintray.com/animeshz/maven")
        maven("https://plugins.gradle.org/m2/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://dl.bintray.com/kotlin/ktor")
        maven("https://central.sonatype.com/repository/maven-snapshots")
//        google {
//            mavenContent {
//                includeGroupAndSubgroups("androidx")
//                includeGroupAndSubgroups("com.android")
//                includeGroupAndSubgroups("com.google")
//            }
//        }
        // old: google() last - every repository above was asked for the Android plugin first
//        google()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
// </editor-fold>------------------------------------------------------------------------------------

// <editor-fold desc="dependency management">--------------------------------------------------------
dependencyResolutionManagement {
    repositories {
        // google first: androidx / com.android / com.google live only there
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://androidx.dev/storage/compose-compiler/repository")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        // old: unfiltered and before google(), so AGP + com.android.tools were looked up on jogamp.org
        // first - its timeouts broke the root project configuration of buildAll
//        maven("https://jogamp.org/deployment/maven")
        // jogamp only serves org.jogamp (gluegen / jogl), never ask it for anything else
        exclusiveContent {
            forRepository { maven("https://jogamp.org/deployment/maven") }
            filter { includeGroupByRegex("org\\.jogamp(\\..*)?") }
        }
        maven("https://jitpack.io")
        maven("https://maven.mozilla.org/maven2/")
        maven("https://maven.pkg.github.com/tuProlog/2p-kt")
        maven("https://dl.bintray.com/animeshz/maven")
        maven("https://plugins.gradle.org/m2/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://dl.bintray.com/kotlin/ktor")
        maven("https://central.sonatype.com/repository/maven-snapshots")
//        google {
//            mavenContent {
//                includeGroupAndSubgroups("androidx")
//                includeGroupAndSubgroups("com.android")
//                includeGroupAndSubgroups("com.google")
//            }
//        }
        // old: google() last - every repository above was asked for the Android plugin first
//        google()
    }
}
// </editor-fold>------------------------------------------------------------------------------------

// <editor-fold desc="includes">---------------------------------------------------------------------
include(":shared")
include(":androidApp")
include(":desktopApp")
include(":compositor")
// old: login screen (greetd greeter) as its own app, packaged as the psippr-greeter deb; the
// desktop app is the login screen again (one deb)
// include(":greeter")
// </editor-fold>------------------------------------------------------------------------------------

// <editor-fold desc="rename gradle build files">----------------------------------------------------
// Configure modules to use their own name as the build file name
// app/build.gradle.kts → app/app.gradle.kts
// features/home/build.gradle.kts → features/home/home.gradle.kts
rootProject.children.forEach { project ->
    fun configureProject(project: ProjectDescriptor) {
        project.buildFileName = "${project.name}.gradle.kts"
        project.children.forEach { child ->
            configureProject(child)
        }
    }
    configureProject(project)
}
// </editor-fold>------------------------------------------------------------------------------------

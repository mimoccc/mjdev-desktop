plugins {
    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.app.icon.generator) apply false
    // auto applied
//    alias(libs.plugins.qodana)
//    alias(libs.plugins.kover)
    alias(libs.plugins.changelog) apply false
    alias(libs.plugins.gradle.ktlint)
    alias(libs.plugins.manes.versions) apply false
    alias(libs.plugins.kotlin.android) apply false
//    alias(libs.plugins.vlc.setup) apply true
}

// vlc setup
//vlcSetup {
//    vlcVersion = "3.0.21"
//    shouldCompressVlcFiles = true
//    shouldIncludeAllVlcFiles = false
//    pathToCopyVlcLinuxFilesTo = rootDir.resolve("resources/linux/")
//    pathToCopyVlcMacosFilesTo = rootDir.resolve("resources/macos/")
//    pathToCopyVlcWindowsFilesTo = rootDir.resolve("resources/windows/")
//}

//dependencies {
//    kover(project(":composeApp"))
//}

//kover { reports { total { xml { onCheck = true } } } }

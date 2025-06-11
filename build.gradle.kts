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
//    id("com.github.gmazzo.buildconfig") version "5.6.5"
}

//buildConfig {
//    generateAtSync = true
//    sourceSets  {
//        main {
//            packageName = "org.mjdev.desktop"
//            useKotlin = true
//            useJava = true
//            useGroovy = true
//            useScala = true
//            useXml = true
//            useProperties = true
//        }
//    }
//    buildConfigField("APP_NAME", project.name)
//    buildConfigField("APP_VERSION", provider { "\"${project.version}\"" })
//    buildConfigField("APP_SECRET", "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu")
//    buildConfigField<String>("OPTIONAL", null)
//    buildConfigField("BUILD_TIME", System.currentTimeMillis())
//    buildConfigField("FEATURE_ENABLED", true)
//    buildConfigField("MAGIC_NUMBERS", intArrayOf(1, 2, 3, 4))
//    buildConfigField("STRING_LIST", arrayOf("a", "b", "c"))
//    buildConfigField("MAP", mapOf("a" to 1, "b" to 2))
//    buildConfigField("FILE", File("aFile"))
//    buildConfigField("URI", uri("https://example.io"))
//    buildConfigField("com.github.gmazzo.buildconfig.demos.kts.SomeData", "DATA", "SomeData(\"a\", 1)")
//}

// vlc setup
//vlcSetup {
//    // todo
//    //val vlcVersionLibs: String = libs.versions.vlc.get().toString()
//    vlcVersion.set("3.0.21-2")
//
//    shouldCompressVlcFiles = true
//    shouldIncludeAllVlcFiles = true
//
//    pathToCopyVlcLinuxFilesTo = rootDir.resolve("composeApp/src/commonMain/composeResources/vlc/")
//    pathToCopyVlcMacosFilesTo = rootDir.resolve("composeApp/src/commonMain/composeResources/vlc/")
//    pathToCopyVlcLinuxFilesTo = rootDir.resolve("composeApp/src/commonMain/composeResources/vlc/")
//}

//dependencies {
//    kover(project(":composeApp"))
//}

//kover { reports { total { xml { onCheck = true } } } }

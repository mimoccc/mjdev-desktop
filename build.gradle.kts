import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
//    alias(libs.plugins.devtools.ksp) apply false
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
    alias(libs.plugins.manes.versions)
    alias(libs.plugins.kotlin.android) apply false
//    id("com.github.gmazzo.buildconfig") version "5.6.5"
}

// TEMP exploratory: stable-only update report across all modules
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return !(stableKeyword || regex.matches(version))
}

allprojects {
    apply(plugin = "com.github.ben-manes.versions")
    tasks.withType<DependencyUpdatesTask> {
        rejectVersionIf { isNonStable(candidate.version) && !isNonStable(currentVersion) }
    }
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

//dependencies {
//    kover(project(":composeApp"))
//}

//kover { reports { total { xml { onCheck = true } } } }

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
//    implementation(libs.gradle)
//    implementation(libs.gradle.api)
//    implementation(libs.gradle.kotlin.plugin)
}

gradlePlugin {
    plugins {
        register("MultiPlatformPlugin") {
            id = "MultiPlatformPlugin"
            displayName = "MultiPlatformPlugin"
            description = "Common library plugin to handle all stuffs needed."
            implementationClass = "org.mjdev.gradle.plugins.MultiPlatformPlugin"
        }
    }
}

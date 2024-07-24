repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(libs.gradle)
    implementation(libs.gradle.api)
}

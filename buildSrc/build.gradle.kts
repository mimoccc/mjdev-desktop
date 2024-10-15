repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
//    java
//    `java-gradle-plugin`
}

dependencies {
//    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
//    implementation(libs.gradle)
//    implementation(libs.gradle.api)
}

//gradlePlugin {
//    plugins {
//        register("DesktopPlugin") {
//            id = "DesktopPlugin"
//            displayName = "DesktopPlugin"
//            description = "Common application plugin to handle all stuffs needed."
//            implementationClass = "org.mjdev.gradle.plugin.DesktopPlugin"
//        }
//    }
//}

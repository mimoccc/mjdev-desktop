plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.app.icon.generator) apply false
    alias(libs.plugins.qodana)
    alias(libs.plugins.kover)
    alias(libs.plugins.changelog)
}
dependencies {
    kover(project(":composeApp"))
    kover(project(":shared"))
}

kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

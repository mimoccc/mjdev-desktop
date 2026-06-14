plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.mjdev.desktop"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()
    // shared resources packaged into the APK (lost when composeApp was split):
    //  - assets  <- commonMain/resources (icon fonts, translations, widgets, …)
    //  - resources <- commonMain/composeResources
    sourceSets["main"].assets.srcDirs(rootDir.resolve("shared/src/commonMain/resources"))
    sourceSets["main"].resources.srcDirs(rootDir.resolve("shared/src/commonMain/composeResources"))
    defaultConfig {
        applicationId = "org.mjdev.desktop"
        minSdk = libs.versions.android.min.sdk.get().toInt()
        targetSdk = libs.versions.android.target.sdk.get().toInt()
        versionCode = libs.versions.app.pkg.version.get().replace(".", "").toInt()
        versionName = libs.versions.app.pkg.version.get()
        resValue("string", "app_name", libs.versions.app.name.get())
    }
    packaging {
        resources {
            excludes += "META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/io.netty.versions.properties"
            pickFirsts += "META-INF/native-image/**"
        }
    }
    buildTypes {
        getByName("release") {
            isDebuggable = true
            isJniDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            isPseudoLocalesEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(projects.shared)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.components.uiToolingPreview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.accompanist.permissions)
    implementation(libs.coil.compose)
    implementation(libs.napier)
    debugImplementation(libs.androidx.ui.tooling)
}

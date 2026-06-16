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
    // Release signing. Keystore + credentials come from env vars (CI secrets) or Gradle
    // properties (local ~/.gradle/gradle.properties) — never hardcoded. When no release
    // keystore is configured we fall back to the debug key so the release APK is still
    // *signed* and installable (an unsigned APK is rejected by Android on install).
    val releaseStoreFile = (providers.environmentVariable("ANDROID_KEYSTORE_FILE").orNull
        ?: providers.gradleProperty("android.keystore.file").orNull)?.let(::file)
    val releaseStorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
        ?: providers.gradleProperty("android.keystore.password").orNull
    val releaseKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
        ?: providers.gradleProperty("android.key.alias").orNull
    val releaseKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
        ?: providers.gradleProperty("android.key.password").orNull
    val hasReleaseKeystore = releaseStoreFile?.exists() == true &&
        !releaseStorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }
    buildTypes {
        getByName("release") {
            isDebuggable = true
            isJniDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            isPseudoLocalesEnabled = true
            // real keystore when provided, otherwise debug key (still a signed, installable APK)
            signingConfig = signingConfigs.getByName(if (hasReleaseKeystore) "release" else "debug")
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

@file:Suppress("PropertyName")

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.multiplatform) apply false
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    maven("https://androidx.dev/storage/compose-compiler/repository")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jogamp.org/deployment/maven")
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
    google()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    compileOnly(libs.gradle)
    compileOnly(libs.gradle.api)
    compileOnly(libs.gradle.kotlin.plugin)
}

gradlePlugin {
    plugins {
        register("MultiPlatformPlugin") {
            id = "MultiPlatformPlugin"
            displayName = "MultiPlatformPlugin"
            description = "Common library plugin to handle all stuffs needed."
            implementationClass = "MultiPlatformPlugin"
        }
    }
}

val FOLDER_BUILD = "build"
val FOLDER_DISTRIBUTION = "distribution"

val PLUGIN_FILE_NAME = "plugin.zip"

val TASK_GROUP = "mjdev"
val TASK_BUILD_PLUGIN = "installPlugin"
val PROJECT_IDE_PLUGIN = "studio-plugin"
val DEPENDENCY_BUILD_SRC = ":$PROJECT_IDE_PLUGIN:$TASK_BUILD_PLUGIN"

fun Task.execute(
    action: Action<ExecSpec>
) = project.providers.exec(action)

val Task.pluginProject
    get() = project.rootProject.project(PROJECT_IDE_PLUGIN)

val Task.pluginDistributionFile
    get() = "${pluginProject.path}/$FOLDER_BUILD/$FOLDER_DISTRIBUTION/$PLUGIN_FILE_NAME"

tasks {
    // clean task enhancement
    clean.configure {
        delete(FOLDER_BUILD)
    }
//    build {
//        finalizedBy(TASK_BUILD_PLUGIN)
//    }
    register(TASK_BUILD_PLUGIN) {
        group = TASK_GROUP
        description = "Builds and installs the IDEA plugin."
        doLast {
            println("Installing IDEA plugin...")
            val pluginFile = file(pluginDistributionFile)
            if (pluginFile.exists()) {
                providers.exec {
                    commandLine(
                        "idea.sh",
                        "installPlugins",
                        pluginFile.absolutePath
                    )
                }
                println("Plugin installed successfully.")
            } else {
                println("Plugin file not found: $pluginDistributionFile")
            }
        }
    }
}

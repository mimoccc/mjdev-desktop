@file:Suppress("PropertyName")

plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    google()
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

val FOLDER_BUILD = "build"
val FOLDER_DISTRIBUTION = "distribution"

val PLUGIN_FILE_NAME = "plugin.zip"

val TASK_GROUP = "mjdev"
val TASK_BUILD_PLUGIN = "installPlugin"
val PROJECT_IDE_PLUGIN = "studio-plugin"
val DEPENDENCY_BUILD_SRC = ":$PROJECT_IDE_PLUGIN:$TASK_BUILD_PLUGIN"

fun Task.execute(action: Action<ExecSpec>) = this.project.providers.exec(action)
val Task.pluginProject
    get() = this.project.rootProject.project(PROJECT_IDE_PLUGIN)
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

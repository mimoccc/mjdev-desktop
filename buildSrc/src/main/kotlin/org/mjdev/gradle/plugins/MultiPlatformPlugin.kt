package org.mjdev.gradle.plugins

import org.gradle.api.Project
import org.mjdev.gradle.tasks.DeployTask
import org.mjdev.gradle.tasks.GenerateChangelogTask
import  org.mjdev.gradle.extensions.ProjectExt.registerTask

class MultiPlatformPlugin : BasePlugin() {
    override fun Project.onConfigure() {
//        buildscript.repositories {
//        }
    }

    override fun Project.registerTasks() {
        registerTask<DeployTask>("deploy")
        registerTask<GenerateChangelogTask>("changelog")
    }

    override fun Project.onBeforeEvaluate() {
    }

    override fun Project.onAfterEvaluate() {
    }
}

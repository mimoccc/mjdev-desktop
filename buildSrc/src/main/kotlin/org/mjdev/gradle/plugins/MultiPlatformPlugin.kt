package org.mjdev.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.mjdev.gradle.tasks.DeployTask
import org.mjdev.gradle.tasks.GenerateChangelogTask
import  org.mjdev.gradle.extensions.ProjectExt.registerTask

class MultiPlatformPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            registerTasks()
            beforeEvaluate {
                onBeforeEvaluate()
            }
            afterEvaluate {
                onAfterEvaluate()
            }
            project.configure(listOf(project)) {
                onConfigure()
            }
        }
    }

    private fun Project.onConfigure() {
//        buildscript.repositories {
//
//        }
    }

    private fun Project.registerTasks() {
        registerTask<DeployTask>("deploy")
        registerTask<GenerateChangelogTask>("changelog")
    }

    private fun Project.onBeforeEvaluate() {

    }

    private fun Project.onAfterEvaluate() {

    }
}

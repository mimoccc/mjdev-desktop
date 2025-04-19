package org.mjdev.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class BasePlugin : Plugin<Project> {
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

    abstract fun Project.onConfigure()
    abstract fun Project.registerTasks()
    abstract fun Project.onBeforeEvaluate()
    abstract fun Project.onAfterEvaluate()
}
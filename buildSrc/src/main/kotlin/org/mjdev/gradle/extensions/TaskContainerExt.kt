package org.mjdev.gradle.extensions

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

object TaskContainerExt {
    fun TaskContainer.clean(
        block: Task.() -> Unit
    ) = findByName("clean")?.apply(block)
}
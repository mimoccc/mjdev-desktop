package org.mjdev.gradle.extensions

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.impldep.jakarta.xml.bind.DatatypeConverter
import org.gradle.kotlin.dsl.extra

@Suppress("HasPlatformType", "unused", "MemberVisibilityCanBePrivate")
object ProjectExt {

    val Project.androidStudioVersion
        get() = project.extra.properties["android.studio.version"]

    val Project.runningFromAndroidStudio
        get() = DatatypeConverter.parseBoolean(project.extra.properties["android.injected.invoked.from.ide"].toString())

    val Project.isAndroidStudio
        get() = project.extra.properties.keys.contains("android.studio.version")

//    val Project.libs
//        get() = the<LibrariesForLibs>()

//    fun Project.apply(plugin: Provider<*>) =
//        project.plugins.apply(plugin.get().toString())

//    fun Project.apply(plugin: String) =
//        project.plugins.apply(plugin)

//    fun <T : Task> Project.createTask(name: String, configureAction: T.() -> Unit) = tasks.create(name).apply {
//        configureAction.invoke(this as T)
//    }

    fun Project.createTask(
        name: String,
        group: String = "mjdev",
        description: String = "",
        configureAction: Task.() -> Unit
    ) = tasks.create(name).apply {
        this.group = group
        this.description = description
        configureAction.invoke(this)
    }

//    inline fun <reified T : Plugin<Project>> Project.apply() =
//        project.plugins.apply(T::class.java)
}
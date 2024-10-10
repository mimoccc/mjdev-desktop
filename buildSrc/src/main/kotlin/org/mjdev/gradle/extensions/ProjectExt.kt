/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.gradle.extensions

//import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.*
//import org.gradle.internal.impldep.jakarta.xml.bind.DatatypeConverter
import org.gradle.kotlin.dsl.*
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate")
object ProjectExt {
    val Project.androidStudioVersion
        get() = project.extra.properties["android.studio.version"]

//    val Project.runningFromAndroidStudio
//        get() = DatatypeConverter.parseBoolean(project.extra.properties["android.injected.invoked.from.ide"].toString())

    val Project.isAndroidStudio
        get() = project.extra.properties.keys.contains("android.studio.version")

//    val Project.libs
//        get() = the<LibrariesForLibs>()

//    val Project.libs: LibrariesForLibs get() =
//        (this as ExtensionAware).extensions.getByName("libs") as org.gradle.accessors.dm.LibrariesForLibs

//    fun Project.libs(configure: Action<LibrariesForLibs>): Unit =
//        (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("libs", configure)

//    val Project.libs : VersionCatalog
//        get() = rootProject.extensions
//            .getByType<VersionCatalogsExtension>().named("libs")

//    inline fun <reified T : Plugin<Project>> Project.apply() =
//        project.plugins.apply(T::class.java)

//    fun Project.apply(plugin: Provider<*>) =
//        project.plugins.apply(plugin.get().toString())

//    fun Project.apply(plugin: String) =
//        project.plugins.apply(plugin)

    inline fun <reified T> Project.configureIfExists(fn: T.() -> Unit) {
        extensions.findByType(T::class.java)?.fn()
    }

    @Suppress("UNUSED_PARAMETER")
    inline fun <reified T : Task> Project.createTask(
        name: String,
        group: String = "mjdev",
        description: String = "",
        className: String? = T::class.simpleName,
        configureAction: T.() -> Unit
    ) = tasks.create<T>(name).apply {
        this.group = group
        this.description = description
        configureAction.invoke(this)
    }

    inline fun Project.createTask(
        name: String,
        group: String = "mjdev",
        description: String = "",
        configureAction: Task.() -> Unit
    ) = tasks.create<Task>(name).apply {
        this.group = group
        this.description = description
        configureAction.invoke(this)
    }

    fun Project.resolve(relative: String): File = rootDir.resolve(File(relative))

//tasks.withType(Copy::class) {
//    from {
//        fileTree("native") {
//            include "*.so", "*.dylib", "*.dll"
//        }
//    }
//    into {
//        configurations.runtimeClasspath.files.collect { it.absolutePath }
//    }
//}

}
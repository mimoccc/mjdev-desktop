/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class DesktopPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        println("Main plugin started for $name")
//        apply(libs.plugins.kotlin.jvm)
//        apply(libs.plugins.compose.desktop)
//        apply(libs.plugins.kotlin.serialization)
//        configureIfExists<DesktopExtension> {
//            application {
//                jvmArgs += "--enable-preview"
//            }
//            nativeApplication {
//            }
//        }
    }
}
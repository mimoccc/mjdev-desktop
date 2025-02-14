package org.mjdev.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

@Suppress("DEPRECATION")
open class DeployTask : DefaultTask() {
    @Input
    var versionName: String = ""
    @Input
    var appName: String = ""
    @Input
    var nexusUrl: String = ""
    @Input
    var nexusUsername: String = ""
    @Input
    var nexusPassword: String = ""
    @Input
    var tagPrefix: String = ""
        get() = field.lowercase()

    private val buildType: String
        get() = if (tagPrefix == "development") "debug" else "release"

    @TaskAction
    fun upload() {
        val apkPath = project.layout
            .projectDirectory
            .file("build/outputs/apk/$buildType/$appName-$versionName-$buildType.apk")
        val changelogPath = project.layout
            .projectDirectory
            .file("build/outputs/apk/$buildType/CHANGELOG-$appName-$versionName-$tagPrefix.md")
        val apkFile = apkPath.asFile
        val changelogFile = changelogPath.asFile
        when {
            !apkFile.exists() -> {
                throw RuntimeException("APK file does not exist: ${apkFile.absolutePath}")
            }
            !changelogFile.exists() -> {
                throw RuntimeException("Changelog file does not exist: ${changelogFile.absolutePath}")
            }
            nexusUrl.isBlank() || nexusUsername.isBlank() || nexusPassword.isBlank() -> {
                throw RuntimeException("Nexus credentials are not set")
            }
            else -> {
                val targetUrl = "$nexusUrl/kompakt-$appName/$versionName/$tagPrefix"
                project.exec {
                    commandLine(
                        "curl",
                        "-v",
                        "-u",
                        "$nexusUsername:$nexusPassword",
                        "--upload-file",
                        apkFile.absolutePath,
                        "$targetUrl/${apkFile.name}"
                    )
                }
                project.exec {
                    commandLine(
                        "curl",
                        "-v",
                        "-u",
                        "$nexusUsername:$nexusPassword",
                        "--upload-file",
                        changelogFile.absolutePath,
                        "$targetUrl/${changelogFile.name}"
                    )
                }
            }
        }
    }
}
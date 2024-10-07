package eu.mjdev.desktop.helpers.system

import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.Custom.consoleOutput
import eu.mjdev.desktop.helpers.exception.ErrorException.Companion.error
import eu.mjdev.desktop.helpers.exception.SuccessException.Companion.success

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Shell(
    block: ShellScope.() -> Unit
) {
    val environment by lazy { Environment() }

    init {
        ShellScope(this).apply(block)
    }

    class ShellScope(
        val shell: Shell,
        val environment: Environment = shell.environment
    ) {
        fun autoStartApps() = shell.autoStartApps()

        fun startApp(
            app: App,
            onStarted: () -> Unit = {},
            onStopped: (e: Throwable) -> Unit = {},
        ) = shell.startApp(app, onStarted, onStopped)

        fun executeAndRead(
            cmd: String,
            vararg args: String
        ): String = executeAndRead(cmd, *args)

        fun executeAndReadLines(
            cmd: String,
            vararg args: String
        ): List<String> = executeAndReadLines(cmd, *args)
    }

    fun autoStartApps() = ProcessBuilder("dex", "-a").apply {
        environment().putAll(environment)
    }.also { processBuilder ->
        runCatching {
            processBuilder.start()

        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    fun startApp(
        app: App,
        onStarted: () -> Unit = {},
        onStopped: (e: Throwable) -> Unit = {},
    ) = ProcessBuilder(
        "dex",
        "-w",
        app.desktopFile?.absolutePath
    ).apply {
        environment().putAll(environment)
    }.also { processBuilder ->
        runCatching<Process> {
            processBuilder.start().let { p ->
                onStarted()
                p.waitFor()
                p
            }
        }.onFailure { e ->
            onStopped(error(e))
        }.onSuccess { p ->
            if (p.exitValue() == 0) {
                onStopped(success(p.consoleOutput))
            } else {
                onStopped(error(p.consoleOutput))
            }
        }
    }

    companion object {
        fun executeAndRead(
            cmd: String,
            vararg args: String
        ): String = Runtime.getRuntime().exec(
            mutableListOf<String>().apply {
                add(cmd)
                addAll(args)
            }.toTypedArray()
        ).apply {
            waitFor()
        }.inputReader().readText()

        fun executeAndReadLines(
            cmd: String,
            vararg args: String
        ): List<String> = Runtime.getRuntime().exec(
            mutableListOf<String>().apply {
                add(cmd)
                addAll(args)
            }.toTypedArray()
        ).apply {
            waitFor()
        }.inputReader().readText().lines()
    }
}
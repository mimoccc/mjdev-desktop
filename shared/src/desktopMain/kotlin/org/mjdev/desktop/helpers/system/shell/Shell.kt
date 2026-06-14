package org.mjdev.desktop.helpers.system.shell

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.desktop.data.App
import org.mjdev.desktop.extensions.Custom.consoleOutput
import org.mjdev.desktop.helpers.exception.ErrorException.Companion.error
import org.mjdev.desktop.helpers.exception.SuccessException.Companion.success
import org.mjdev.desktop.helpers.system.environment.Environment
import org.mjdev.desktop.helpers.system.environment.EnvironmentStub
import org.mjdev.desktop.log.Log

@Suppress("MemberVisibilityCanBePrivate", "unused", "RedundantSuspendModifier")
class Shell(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val environment: EnvironmentStub = Environment(),
    block: suspend ShellScope.() -> Unit,
) {
    init {
        scope.launch(Dispatchers.Default) {
            with(ShellScope(this@Shell)) {
                block()
            }
        }
    }

    suspend fun autoStartApps() =
        ProcessBuilder(
            CMD_DEX,
            "-a",
        ).apply {
            environment().putAll(environment.toMap())
            redirectErrorStream(true)
        }.also { processBuilder ->
            runCatching {
                processBuilder.start()
            }.onFailure { e ->
                Log.e(e)
            }
        }

    suspend fun startApp(
        app: App,
        onStarted: () -> Unit = {},
        onStopped: (e: Throwable) -> Unit = {},
    ) = ProcessBuilder(
        CMD_DEX,
        "-w",
        app.desktopFile.absolutePath,
    ).apply {
        environment().putAll(environment.toMap())
        redirectErrorStream(true)
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
        const val CMD_DEX = "dex"

        fun executeAndRead(
            cmd: String,
            vararg args: String,
            onError: (Throwable) -> Unit = { e -> Log.e(e) },
        ): String =
            runCatching {
                Runtime
                    .getRuntime()
                    .exec(arrayOf(cmd) + args)
                    .apply {
                        waitFor()
                    }.onError { e ->
                        onError(e)
                    }
            }.onFailure { e ->
                onError(e)
            }.getOrNull()
                ?.inputReader()
                ?.readText()
                .orEmpty()

        fun execute(
            cmd: String,
            vararg args: String,
        ): Result<Process> =
            runCatching {
                Runtime
                    .getRuntime()
                    .exec(arrayOf(cmd) + args)
                    .apply {
                        waitFor()
                    }.onError { e ->
                        throw (e)
                    }
            }

        fun executeAndReadLines(
            cmd: String,
            vararg args: String,
            onError: (Throwable) -> Unit = { e -> Log.e(e) },
        ): List<String> =
            runCatching {
                Runtime
                    .getRuntime()
                    .exec(arrayOf(cmd) + args)
                    .apply {
                        waitFor()
                    }.onError { e ->
                        onError(e)
                    }
            }.onFailure { e ->
                onError(e)
            }.getOrNull()
                ?.inputReader()
                ?.readLines() ?: emptyList()

        fun Process.onText(onText: (String) -> Unit): Process {
            if (inputStream.available() > 0) {
                onText(inputReader().readText())
            }
            return this
        }

        fun Process.onError(onError: (Throwable) -> Unit): Process {
            if (errorStream.available() > 0) {
                onError(RuntimeException(errorReader().readText()))
            }
            return this
        }
    }
}

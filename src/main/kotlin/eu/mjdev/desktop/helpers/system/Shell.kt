package eu.mjdev.desktop.helpers.system

import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.Custom.consoleOutput
import eu.mjdev.desktop.helpers.exception.ErrorException.Companion.error
import eu.mjdev.desktop.helpers.exception.SuccessException.Companion.success
import eu.mjdev.desktop.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("MemberVisibilityCanBePrivate", "unused", "RedundantSuspendModifier")
class Shell(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    block: suspend ShellScope.() -> Unit
) {
    val environment by lazy { Environment() }

    init {
        scope.launch(Dispatchers.IO) {
            with(ShellScope(this@Shell)) {
                block()
            }
        }
    }

    class ShellScope(
        val shell: Shell,
        val environment: Environment = shell.environment
    ) {
        suspend fun autoStartApps() = shell.autoStartApps()

        suspend fun startApp(
            app: App,
            onStarted: () -> Unit = {},
            onStopped: (e: Throwable) -> Unit = {},
        ) = shell.startApp(app, onStarted, onStopped)

        suspend fun executeAndRead(
            cmd: String,
            vararg args: String
        ): String = executeAndRead(cmd, *args)

        suspend fun executeAndReadLines(
            cmd: String,
            vararg args: String
        ): List<String> = executeAndReadLines(cmd, *args)
    }

    suspend fun autoStartApps() = ProcessBuilder(
        CMD_DEX,
        "-a"
    ).apply {
        environment().putAll(environment)
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
        app.desktopFile.absolutePath
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

        const val CMD_NMCLI = "nmcli"
        val CMD_NMCLI_RESCAN = arrayOf("dev", "wifi", "rescan")
        val CMD_NMCLI_GET_NETWORKS = arrayOf("-t", "-f", "ALL", "dev", "wifi")

        const val CMD_ENV = "env"
        const val CMD_DEX = "dex"

        fun executeAndRead(
            cmd: String,
            vararg args: String,
            onError: (Throwable) -> Unit = { e -> Log.e(e) }
        ): String = runCatching {
            Runtime.getRuntime().exec(arrayOf(cmd) + args).apply {
                waitFor()
            }.onError { e ->
                onError(e)
            }
        }.onFailure { e ->
            onError(e)
        }.getOrNull()?.inputReader()?.readText().orEmpty()

        fun executeAndReadLines(
            cmd: String,
            vararg args: String,
            onError: (Throwable) -> Unit = { e -> Log.e(e) }
        ): List<String> = runCatching {
            Runtime.getRuntime().exec(arrayOf(cmd) + args).apply {
                waitFor()
            }.onError { e ->
                onError(e)
            }
        }.onFailure { e ->
            onError(e)
        }.getOrNull()?.inputReader()?.readLines() ?: emptyList()

        fun Process.onText(
            onText: (String) -> Unit
        ): Process {
            if (inputStream.available() > 0) {
                onText(inputReader().readText())
            }
            return this
        }

        fun Process.onError(
            onError: (Throwable) -> Unit
        ): Process {
            if (errorStream.available() > 0) {
                onError(RuntimeException(errorReader().readText()))
            }
            return this
        }
    }
}

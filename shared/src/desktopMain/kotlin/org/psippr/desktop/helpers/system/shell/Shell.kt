package org.psippr.desktop.helpers.system.shell

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.psippr.desktop.data.App
import org.psippr.desktop.extensions.Custom.consoleOutput
import org.psippr.desktop.helpers.exception.ErrorException.Companion.error
import org.psippr.desktop.helpers.exception.SuccessException.Companion.success
import org.psippr.desktop.helpers.system.environment.Environment
import org.psippr.desktop.helpers.system.environment.EnvironmentStub
import org.psippr.desktop.log.Log

@Suppress("MemberVisibilityCanBePrivate", "unused", "RedundantSuspendModifier")
class Shell(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val environment: EnvironmentStub = Environment(),
    block: suspend ShellScope.() -> Unit,
) {
    init {
        // The block runs blocking process I/O (ProcessBuilder.start/waitFor) — keep it on IO so it
        // never ties up the CPU-sized Default pool.
        scope.launch(Dispatchers.IO) {
            with(ShellScope(this@Shell)) {
                block()
            }
        }
    }

    suspend fun autoStartApps() = ProcessBuilder(
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

    // old: blocked a Dispatchers.Default thread in waitFor() for the app's whole life (8 running
    // apps on 8 cores = no app could start any more) and never read the output pipe until exit,
    // so a chatty app froze once the pipe buffer was full
//    suspend fun startApp(
//        app: App,
//        onStarted: () -> Unit = {},
//        onStopped: (e: Throwable) -> Unit = {},
//    ) = ProcessBuilder(
//        CMD_DEX,
//        "-w",
//        app.desktopFile.absolutePath,
//    ).apply {
//        environment().putAll(environment.toMap())
//        redirectErrorStream(true)
//    }.also { processBuilder ->
//        runCatching<Process> {
//            processBuilder.start().let { p ->
//                onStarted()
//                p.waitFor()
//                p
//            }
//        }.onFailure { e ->
//            onStopped(error(e))
//        }.onSuccess { p ->
//            if (p.exitValue() == 0) {
//                onStopped(success(p.consoleOutput))
//            } else {
//                onStopped(error(p.consoleOutput))
//            }
//        }
//    }

    /**
     * Starts [app] via dex and suspends until it exits, without holding a thread: the exit is
     * awaited, the output goes to the session log instead of an unread pipe.
     */
    suspend fun startApp(
        app: App,
        onStarted: () -> Unit = {},
        onStopped: (e: Throwable) -> Unit = {},
    ) {
        val builder =
            ProcessBuilder(
                CMD_DEX,
                "-w",
                app.desktopFile.absolutePath,
            ).apply {
                environment().putAll(environment.toMap())
                redirectErrorStream(true)
                redirectOutput(ProcessBuilder.Redirect.INHERIT)
            }
        runCatching {
            val process = withContext(Dispatchers.IO) { builder.start() }
            onStarted()
            process.onExit().await()
        }.onFailure { e ->
            onStopped(error(e))
        }.onSuccess { process ->
            val exit = "$EXIT_CODE_LABEL${process.exitValue()}"
            if (process.exitValue() == 0) onStopped(success(exit)) else onStopped(error(exit))
        }
    }

    companion object {
        const val CMD_DEX = "dex"

        /** Prefix of the stop message carrying the launched app's exit code. */
        private const val EXIT_CODE_LABEL = "exit code "

        fun executeAndRead(
            cmd: String,
            vararg args: String,
            onError: (Throwable) -> Unit = { e -> Log.e(e) },
        ): String = runCatching {
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
        ): Result<Process> = runCatching {
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
        ): List<String> = runCatching {
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

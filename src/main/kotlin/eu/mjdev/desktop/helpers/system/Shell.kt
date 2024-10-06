package eu.mjdev.desktop.helpers.system

import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.helpers.exception.ErrorException.Companion.error
import eu.mjdev.desktop.helpers.exception.SuccessException.Companion.success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.util.*

private val Process.consoleOutput: String
    get() {
        val sj = StringJoiner(System.lineSeparator())
        val bfr = BufferedReader(inputReader())
        bfr.lines().iterator().forEachRemaining { s: String? -> sj.add(s) }
        return sj.toString()
    }

@Suppress("MemberVisibilityCanBePrivate", "unused")
object Shell {

    val environment: Environment
        get() = Environment()

    val Process.handle: ProcessHandle
        get() = toHandle()

    fun autoStartApps() = CoroutineScope(Dispatchers.IO).launch {
        ProcessBuilder("dex", "-a").apply {
            environment().putAll(environment)
        }.also { processBuilder ->
            runCatching {
                processBuilder.start()
            }.onFailure { e ->
                e.printStackTrace()
            }
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

    fun executeAndRead(
        cmd: String,
        vararg args: String
    ): String =
        Runtime.getRuntime().exec(
            mutableListOf<String>().apply {
                add(cmd)
                addAll(args)
            }.toTypedArray()
        ).apply {
            waitFor()
        }.inputReader().readText()

    fun executeAndReadLines(cmd: String, vararg args: String): List<String> =
        Runtime.getRuntime().exec(
            mutableListOf<String>().apply {
                add(cmd)
                addAll(args)
            }.toTypedArray()
        ).apply {
            waitFor()
        }.inputReader().readText().lines()
}

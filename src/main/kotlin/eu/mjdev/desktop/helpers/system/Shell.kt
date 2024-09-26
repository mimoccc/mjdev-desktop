package eu.mjdev.desktop.helpers.system

import eu.mjdev.desktop.extensions.Custom.command
import eu.mjdev.desktop.provider.DesktopProvider
import java.io.BufferedReader
import java.io.BufferedWriter

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter", "unused")
class Shell(
    val api: DesktopProvider,
    val shellType: ShellType = ShellType.SH,
) : AutoCloseable {
    val environment: Environment = Environment()
    val process: Process = Runtime.getRuntime().exec(
        arrayOf(shellType.cmd),
        environment.toTypedArray()
    )
    val input: BufferedReader = process.inputReader()
    val output: BufferedWriter = process.outputWriter()

    val isAlive: Boolean
        get() = process.isAlive

    val pid
        get() = process.pid()

    val pids
        get() = getProcessPids()

    val command: String
        get() = process.command ?: ""

    val allProcesses: List<ProcessHandle>
        get() = ProcessHandle.allProcesses().toList()

    val allProcessesPids
        get() = allProcesses.map { it.pid() }

    val childProcesses: List<ProcessHandle>
        get() = process.descendants().toList()

//    val allWindows
//        get() = api.windows.allSystemWindows

//    val allWindowPids
//        get() = allWindows.map { it.pid }.toList()

//    val processWindows
//        get() = allWindows.filter { pids.contains(it.pid) }

//    val processWindowsPids
//        get() = processWindows.map { it.pid }

//    val hasWindow: Boolean
//        get() = processWindows.isNotEmpty()

//    val isRunning: Boolean = hasWindow

//    fun isWindowFocus(api: DesktopProvider): Boolean =
//        api.windows.isWindowActive(pids)

//    fun hasWindow(api: DesktopProvider) =
//        api.windows.getWindowsByPids(pids).isNotEmpty()

//    fun requestWindowFocus(api: DesktopProvider) {
//        val windows = api.windows.getWindowsByPids(pids)
//        if (windows.isNotEmpty()) {
//            windows.forEach { w -> w.toFront() }
//        }
//    }

//    fun minimizeWindow(api: DesktopProvider) {
//        val windows = api.windows.getWindowsByPids(pids)
//        if (windows.isNotEmpty()) {
//            windows.forEach { w -> w.minimize() }
//        }
//    }

//    fun maximizeWindow(api: DesktopProvider) {
//        val windows = api.windows.getWindowsByPids(pids)
//        if (windows.isNotEmpty()) {
//            windows.forEach { w -> w.maximize() }
//        }
//    }

//    fun closeWindow(api: DesktopProvider) {
//        val windows = api.windows.getWindowsByPids(pids)
//        if (windows.isNotEmpty()) {
//            windows.forEach { w -> w.close() }
//        } else {
//            close()
//        }
//    }

    fun onExit(
        block: (Shell) -> Unit
    ): Shell {
        waitFor(block)
        return this
    }

    fun getProcessPids(): List<Long> = mutableListOf<Long>().apply {
        add(process.pid())
        addAll(process.descendants().map { it.pid() }.toList())
    }.distinct()

    fun toHandle(): ProcessHandle =
        process.toHandle()

    fun waitFor(
        block: (Shell) -> Unit
    ): Shell {
//        while (hasWindow) {
//            process.waitFor()
//        }
        return apply {
            block(this)
            close()
        }
    }

    fun writeCommand(cmd: String): Shell {
        output.write(cmd + "\n")
        output.flush()
        return this
    }

    fun writeCommand(cmd: String, vararg args: String): Shell {
        output.write(cmd + args.joinToString { " $it " } + "\n")
        output.flush()
        return this
    }

    fun readOutput(): String =
        input.readText()

    fun readOutputLines(): List<String> = input.readLines().filter {
        it.isNotEmpty()
    }

    fun readWhileDone(): Shell {
        process.waitFor()
        return this
    }

    override fun close() =
        process.destroy()

    companion object {
        fun executeAndRead(cmd: String, vararg args: String): String =
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

    enum class ShellType(
        val cmd: String
    ) {
        SH("/bin/sh"),
        BASH("/bin/bash")
    }
}
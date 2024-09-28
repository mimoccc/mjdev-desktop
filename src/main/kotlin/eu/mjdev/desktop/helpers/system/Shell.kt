package eu.mjdev.desktop.helpers.system

import eu.mjdev.desktop.extensions.Custom.command
import eu.mjdev.desktop.extensions.Custom.readAvailable
import eu.mjdev.desktop.provider.DesktopProvider
import java.io.BufferedReader
import java.io.BufferedWriter

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter", "unused")
class Shell(
    val api: DesktopProvider,
    val shellType: ShellType = ShellType.SH,
) {
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

    fun getProcessPids(): List<Long> = mutableListOf<Long>().apply {
        add(process.pid())
        addAll(process.descendants().map { it.pid() }.toList())
    }.distinct()

    fun toHandle(): ProcessHandle =
        process.toHandle()

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
        input.readAvailable()

    fun readOutputLines(): List<String> = input.readLines().filter {
        it.isNotEmpty()
    }

    fun readWhileDone(): Shell {
        process.waitFor()
        return this
    }

    fun close(): Shell {
        process.destroy()
        return this
    }

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

package org.psippr.desktop.managers.devices.adb

import org.psippr.desktop.log.Log

/**
 * Runs the `adb` command (package `adb`). Every call is limited by coreutils `timeout`, so a
 * device that stops answering never blocks the devices tab; output is read while the process
 * runs, so a big answer (a screenshot) cannot block on a full pipe.
 */
object AdbCli {
    /** The adb command. */
    const val ADB = "adb"

    /** Option selecting one device by its serial. */
    const val SERIAL_OPTION = "-s"

    /** coreutils command ending a call that runs longer than its limit. */
    private const val TIMEOUT = "timeout"

    /** Seconds a query may take. */
    const val QUERY_TIMEOUT_S = 8

    /** Seconds a transfer or install may take. */
    const val TRANSFER_TIMEOUT_S = 600

    /** Answer of a call: its exit code and output (stdout and stderr, as text). */
    data class Answer(
        val exitCode: Int,
        val lines: List<String>,
    ) {
        /** True when adb exited with 0. */
        val ok: Boolean
            get() = exitCode == 0
    }

    /** Runs `adb [-s serial] args` for at most [timeoutS] seconds. */
    fun run(
        serial: String?,
        vararg args: String,
        timeoutS: Int = QUERY_TIMEOUT_S,
    ): Answer = runCatching {
        val process =
            ProcessBuilder(command(serial, timeoutS, *args))
                .redirectErrorStream(true)
                .start()
        val text = process.inputStream.bufferedReader().readText()
        Answer(process.waitFor(), text.lines())
    }.onFailure { e -> Log.w("adb ${args.joinToString(" ")} failed: ${e.message}") }
        .getOrElse { Answer(NOT_STARTED, emptyList()) }

    /** Raw stdout of `adb -s serial args` (binary, e.g. a PNG), null when it failed. */
    fun bytes(
        serial: String,
        vararg args: String,
        timeoutS: Int = QUERY_TIMEOUT_S,
    ): ByteArray? = runCatching {
        val process = ProcessBuilder(command(serial, timeoutS, *args)).start()
        val bytes = process.inputStream.readAllBytes()
        if (process.waitFor() == 0 && bytes.isNotEmpty()) bytes else null
    }.getOrNull()

    private fun command(
        serial: String?,
        timeoutS: Int,
        vararg args: String,
    ): List<String> = buildList {
        add(TIMEOUT)
        add(timeoutS.toString())
        add(ADB)
        if (serial != null) {
            add(SERIAL_OPTION)
            add(serial)
        }
        addAll(args)
    }

    /** Exit code used when adb could not be started at all. */
    private const val NOT_STARTED = -1
}

package org.mjdev.desktop.managers.volume.pactl

import java.io.BufferedReader

/** Runs `pactl` (PulseAudio / PipeWire CLI, already a runtime dependency) in the C locale. */
object Pactl {
    /** pactl binary. */
    private const val CMD = "pactl"

    /** Locale variable forced so output is parsable whatever the user's language. */
    private const val ENV_LC_ALL = "LC_ALL"

    /** C locale (English, stable output). */
    private const val LOCALE_C = "C"

    /** stdout of `pactl args`, empty on failure. */
    fun run(vararg args: String): String = runCatching {
        val process =
            ProcessBuilder(CMD, *args)
                .redirectErrorStream(false)
                .apply { environment()[ENV_LC_ALL] = LOCALE_C }
                .start()
        val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
        process.waitFor()
        output
    }.getOrDefault("")
}

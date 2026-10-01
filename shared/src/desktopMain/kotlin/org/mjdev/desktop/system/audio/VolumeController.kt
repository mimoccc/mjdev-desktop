package org.mjdev.desktop.system.audio

import java.io.BufferedReader

/**
 * Desktop (Linux/JVM) implementation of [VolumeController].
 * Uses `pactl` (PulseAudio), a system tool already required by the desktop
 * runtime dependencies - no external library is introduced.
 */
actual object VolumeController {
    // Default PulseAudio sink used when no specific sink is selected.
    private const val DEFAULT_SINK = "@DEFAULT_SINK@"

    // Regex used to parse the percentage value out of `pactl get-sink-volume` output.
    private val VOLUME_REGEX = Regex("(\\d+)%")

    actual fun getVolume(): Float = runCatching {
        val process = ProcessBuilder("pactl", "get-sink-volume", DEFAULT_SINK).start()
        val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
        process.waitFor()
        VOLUME_REGEX
            .find(output)
            ?.groupValues
            ?.get(1)
            ?.toFloatOrNull()
            ?.div(100f)
    }.getOrNull() ?: 0f

    actual fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        val percent = (clamped * 100).toInt()
        runCatching {
            ProcessBuilder("pactl", "set-sink-volume", DEFAULT_SINK, "$percent%").start().waitFor()
        }
    }

    actual fun isMuted(): Boolean = runCatching {
        val process = ProcessBuilder("pactl", "get-sink-mute", DEFAULT_SINK).start()
        val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
        process.waitFor()
        output.contains("yes", ignoreCase = true)
    }.getOrNull() ?: false

    actual fun toggleMute(): Boolean {
        runCatching {
            ProcessBuilder("pactl", "set-sink-mute", DEFAULT_SINK, "toggle").start().waitFor()
        }
        return isMuted()
    }
}

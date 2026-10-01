package org.mjdev.desktop.managers.volume

import org.mjdev.desktop.context.IDesktopContext
import java.io.BufferedReader

/**
 * Desktop (Linux/JVM) implementation of [IVolumeManager].
 * Uses `pactl` (PulseAudio), a system tool already required by the desktop
 * runtime dependencies - no external library is introduced.
 */
class VolumeManager(
    private val context: IDesktopContext,
) : IVolumeManager {
    companion object {
        // Default PulseAudio sink used when no specific sink is selected.
        private const val DEFAULT_SINK = "@DEFAULT_SINK@"

        // Regex used to parse the percentage value out of `pactl get-sink-volume` output.
        private val VOLUME_REGEX = Regex("(\\d+)%")
    }

    override val volume: Float
        get() =
            runCatching {
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

    override val isMuted: Boolean
        get() =
            runCatching {
                val process = ProcessBuilder("pactl", "get-sink-mute", DEFAULT_SINK).start()
                val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
                process.waitFor()
                output.contains("yes", ignoreCase = true)
            }.getOrNull() ?: false

    override fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        val percent = (clamped * 100).toInt()
        runCatching {
            ProcessBuilder("pactl", "set-sink-volume", DEFAULT_SINK, "$percent%").start().waitFor()
        }
    }

    override fun toggleMute(): Boolean {
        runCatching {
            ProcessBuilder("pactl", "set-sink-mute", DEFAULT_SINK, "toggle").start().waitFor()
        }
        return isMuted
    }
}

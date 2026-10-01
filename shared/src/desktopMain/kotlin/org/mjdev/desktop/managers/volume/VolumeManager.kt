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

        // pactl arguments used to discover the output devices.
        private const val CMD_GET_DEFAULT_SINK = "get-default-sink"
        private val CMD_LIST_SINKS = arrayOf("list", "short", "sinks")

        // Column of the sink name in `pactl list short sinks` (index, name, module, ...).
        private const val SINK_NAME_COLUMN = 1
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

    override val outputs: List<AudioOutput>
        get() {
            val default = runCatching { runPactl(CMD_GET_DEFAULT_SINK).trim() }.getOrDefault("")
            return runCatching { runPactl(*CMD_LIST_SINKS) }
                .getOrDefault("")
                .lineSequence()
                .mapNotNull { line -> line.split('\t').getOrNull(SINK_NAME_COLUMN)?.takeIf { it.isNotBlank() } }
                .map { name -> AudioOutput(name = name, isDefault = name == default) }
                .toList()
        }

    override fun setDefaultOutput(name: String) {
        runCatching {
            ProcessBuilder("pactl", "set-default-sink", name).start().waitFor()
        }
    }

    private fun runPactl(vararg args: String): String {
        val process = ProcessBuilder("pactl", *args).start()
        val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
        process.waitFor()
        return output
    }

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

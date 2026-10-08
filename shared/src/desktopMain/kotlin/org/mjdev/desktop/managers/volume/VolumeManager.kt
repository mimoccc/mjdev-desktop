package org.mjdev.desktop.managers.volume

import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.volume.pactl.Pactl
import org.mjdev.desktop.managers.volume.pactl.PactlDevices
import org.mjdev.desktop.managers.volume.pactl.PactlEvents
import org.mjdev.desktop.managers.volume.pactl.PactlParser
import org.mjdev.desktop.managers.volume.pactl.PactlTarget
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

        // pactl arguments listing the sound cards (profiles).
        private val CMD_LIST_CARDS = arrayOf("list", "cards")

        // pactl verbs used by the device / profile API.
        private const val CMD_LIST = "list"
        private const val CMD_SET_CARD_PROFILE = "set-card-profile"
        private const val MUTE_TOGGLE = "toggle"
        private const val MUTE_YES = "yes"

        // Volume is passed to / read from pactl in percent.
        private const val PERCENT = 100f
    }

    // C locale fix: the old readers below parsed the user's locale ("Ztlumení: ano" never contains "yes")
    override val volume: Float
        get() = volume(AudioDeviceKind.OUTPUT)

    override val isMuted: Boolean
        get() = isMuted(AudioDeviceKind.OUTPUT)

    // old volume reader, replaced by volume(AudioDeviceKind)
    @Suppress("unused")
    private val volumeOld: Float
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

    // old mute reader (locale dependent), replaced by isMuted(AudioDeviceKind)
    @Suppress("unused")
    private val isMutedOld: Boolean
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

    override fun devices(kind: AudioDeviceKind): List<AudioDevice> {
        val target = PactlTarget.of(kind)
        val default = Pactl.run(target.getDefault).trim()
        return PactlDevices.devices(PactlParser.parse(Pactl.run(CMD_LIST, target.listType)), target, default)
    }

    override fun setDefaultDevice(device: AudioDevice) {
        val target = PactlTarget.of(device.kind)
        Pactl.run(target.setDefault, device.id)
        device.port?.let { port -> Pactl.run(target.setPort, device.id, port) }
    }

    override fun volume(kind: AudioDeviceKind): Float {
        val target = PactlTarget.of(kind)
        return VOLUME_REGEX
            .find(Pactl.run(target.getVolume, target.defaultAlias))
            ?.groupValues
            ?.get(1)
            ?.toFloatOrNull()
            ?.div(PERCENT) ?: 0f
    }

    override fun setVolume(
        kind: AudioDeviceKind,
        volume: Float,
    ) {
        val target = PactlTarget.of(kind)
        val percent = (volume.coerceIn(0f, 1f) * PERCENT).toInt()
        Pactl.run(target.setVolume, target.defaultAlias, "$percent%")
    }

    override fun isMuted(kind: AudioDeviceKind): Boolean {
        val target = PactlTarget.of(kind)
        return Pactl.run(target.getMute, target.defaultAlias).contains(MUTE_YES, ignoreCase = true)
    }

    override fun toggleMute(kind: AudioDeviceKind): Boolean {
        val target = PactlTarget.of(kind)
        Pactl.run(target.setMute, target.defaultAlias, MUTE_TOGGLE)
        return isMuted(kind)
    }

    override val profiles: List<AudioProfile>
        get() = PactlDevices.profiles(PactlParser.parse(Pactl.run(*CMD_LIST_CARDS)))

    override fun setProfile(profile: AudioProfile) {
        Pactl.run(CMD_SET_CARD_PROFILE, profile.cardId, profile.id)
    }

    override fun watchChanges(onChange: () -> Unit): AutoCloseable = PactlEvents.start(onChange)

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

package org.mjdev.desktop.managers.volume

import org.mjdev.desktop.managers.base.IDelegate

/**
 * Manager abstraction for controlling the system output volume and mute state.
 * Follows the same pattern as [org.mjdev.desktop.managers.os.IOSManager] - a plain
 * interface implemented per-platform and wired via [org.mjdev.desktop.managers.base.ManagerCache].
 */
interface IVolumeManager : IDelegate {
    /** Current volume level in range 0f..1f. */
    val volume: Float

    /** True if audio output is currently muted. */
    val isMuted: Boolean

    /** Sets volume level, clamped to 0f..1f. */
    fun setVolume(volume: Float)

    /** Toggles mute state and returns the new state. */
    fun toggleMute(): Boolean

    /** Audio output devices known to the system; empty when the platform cannot list them. */
    val outputs: List<AudioOutput>
        get() = emptyList()

    /** Makes the output named [name] the default one. No-op where unsupported. */
    fun setDefaultOutput(name: String) {}

    /** Selectable devices of [kind] with human readable names; empty where unsupported. */
    fun devices(kind: AudioDeviceKind): List<AudioDevice> = emptyList()

    /** Makes [device] (and its port) the default of its kind. No-op where unsupported. */
    fun setDefaultDevice(device: AudioDevice) {}

    /** Volume of the default device of [kind] in range 0f..1f. */
    fun volume(kind: AudioDeviceKind): Float = if (kind == AudioDeviceKind.OUTPUT) volume else 0f

    /** Sets the volume of the default device of [kind], clamped to 0f..1f. */
    fun setVolume(
        kind: AudioDeviceKind,
        volume: Float,
    ) {
        if (kind == AudioDeviceKind.OUTPUT) setVolume(volume)
    }

    /** True if the default device of [kind] is muted. */
    fun isMuted(kind: AudioDeviceKind): Boolean = if (kind == AudioDeviceKind.OUTPUT) isMuted else false

    /** Toggles mute of the default device of [kind] and returns the new state. */
    fun toggleMute(kind: AudioDeviceKind): Boolean = if (kind == AudioDeviceKind.OUTPUT) toggleMute() else false

    /** Selectable profiles (configurations) of the sound cards; empty where unsupported. */
    val profiles: List<AudioProfile>
        get() = emptyList()

    /** Activates [profile] on its card. No-op where unsupported. */
    fun setProfile(profile: AudioProfile) {}

    companion object {
        /** No-op fallback used when no platform manager is available. */
        val EMPTY =
            object : IVolumeManager {
                override val volume: Float = 0f
                override val isMuted: Boolean = false

                override fun setVolume(volume: Float) {}

                override fun toggleMute(): Boolean = false
            }
    }
}

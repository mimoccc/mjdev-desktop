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

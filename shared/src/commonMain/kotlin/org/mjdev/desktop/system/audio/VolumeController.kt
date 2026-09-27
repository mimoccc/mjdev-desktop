package org.mjdev.desktop.system.audio

/**
 * Platform volume controller abstraction.
 * Implemented per-platform (desktop via `pactl`, Android via `AudioManager`)
 * without any external or remote library dependency.
 */
expect object VolumeController {
    /** Returns current volume level in range 0f..1f. */
    fun getVolume(): Float

    /** Sets volume level, clamped to 0f..1f. */
    fun setVolume(volume: Float)

    /** Returns true if audio output is currently muted. */
    fun isMuted(): Boolean

    /** Toggles mute state and returns the new state. */
    fun toggleMute(): Boolean
}

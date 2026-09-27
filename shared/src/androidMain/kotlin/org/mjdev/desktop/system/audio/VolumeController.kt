package org.mjdev.desktop.system.audio

import android.content.Context
import android.media.AudioManager

/**
 * Android implementation of [VolumeController], backed by [AudioManager].
 * Must be initialized once with an application [Context] via [initialize]
 * before any volume operation is performed, e.g. from Application.onCreate().
 */
actual object VolumeController {
    // Application context set once at startup, used to obtain the system AudioManager.
    private var appContext: Context? = null

    // Lazily resolved AudioManager instance targeting the music stream.
    private val audioManager: AudioManager?
        get() = appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    /**
     * Initializes the controller with an application context.
     * Must be called once before any volume operation on Android.
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    actual fun getVolume(): Float {
        val manager = audioManager ?: return 0f
        val current = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return if (max == 0) 0f else current.toFloat() / max.toFloat()
    }

    actual fun setVolume(volume: Float) {
        val manager = audioManager ?: return
        val max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = (volume.coerceIn(0f, 1f) * max).toInt()
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
    }

    actual fun isMuted(): Boolean {
        val manager = audioManager ?: return false
        return manager.isStreamMute(AudioManager.STREAM_MUSIC)
    }

    actual fun toggleMute(): Boolean {
        val manager = audioManager ?: return false
        val newState = !isMuted()
        manager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (newState) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
            0,
        )
        return newState
    }
}

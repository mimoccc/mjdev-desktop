package org.mjdev.desktop.managers.volume

import android.media.AudioManager
import org.mjdev.desktop.context.DesktopContext
import org.mjdev.desktop.context.IDesktopContext

/**
 * Android implementation of [IVolumeManager], backed by [AudioManager].
 * Obtains the Android [android.content.Context] from [DesktopContext.androidContext],
 * the same way other android-specific managers reach platform APIs.
 */
class VolumeManager(
    private val context: IDesktopContext,
) : IVolumeManager {
    private val audioManager: AudioManager?
        get() =
            (context as? DesktopContext)
                ?.androidContext
                ?.getSystemService(AudioManager::class.java)

    override val volume: Float
        get() {
            val manager = audioManager ?: return 0f
            val current = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            return if (max == 0) 0f else current.toFloat() / max.toFloat()
        }

    override val isMuted: Boolean
        get() = audioManager?.isStreamMute(AudioManager.STREAM_MUSIC) ?: false

    override fun setVolume(volume: Float) {
        val manager = audioManager ?: return
        val max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = (volume.coerceIn(0f, 1f) * max).toInt()
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
    }

    override fun toggleMute(): Boolean {
        val manager = audioManager ?: return false
        val newState = !isMuted
        manager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (newState) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
            0,
        )
        return newState
    }
}

package org.mjdev.desktop.components.video

import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.base.MediaPlayer as VlcMediaPlayer

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MediaPlayer(
    private val mediaPlayer: VlcMediaPlayer,
) {
    actual fun play() {
        mediaPlayer.controls().play()
    }

    actual fun pause() {
        mediaPlayer.controls().pause()
    }

    actual val isPlaying: Boolean
        get() = mediaPlayer.status().isPlaying

    actual fun setRate(rate: Float) {
        mediaPlayer.controls().setRate(rate)
    }

    actual fun setTime(millis: Long) {
        mediaPlayer.controls().setTime(millis)
    }

    actual fun setTimeAccurate(millis: Long) {
        mediaPlayer.controls().setTime(millis)
    }

    actual fun getTimeMillis(): Long = mediaPlayer.status().time()

    actual fun getLengthMillis(): Long = mediaPlayer.status().length()

    actual fun addOnTimeChangedListener(listener: OnTimeChangedListener) {
        mediaPlayer.events().addMediaPlayerEventListener(
            object : MediaPlayerEventAdapter() {
                override fun timeChanged(
                    mediaPlayer: uk.co.caprica.vlcj.player.base.MediaPlayer?,
                    newTime: Long,
                ) {
                    super.timeChanged(mediaPlayer, newTime)
                    listener.onTimeChanged(newTime)
                }
            },
        )
    }

    actual fun dispose() {
        mediaPlayer.release()
    }
}

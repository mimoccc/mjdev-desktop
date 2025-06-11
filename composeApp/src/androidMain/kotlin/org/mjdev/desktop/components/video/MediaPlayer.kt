package org.mjdev.desktop.components.video

import android.media.MediaPlayer
import android.os.Build
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MediaPlayer(
    private val mediaPlayer: MediaPlayer
) {
    private val mediaPlayerCoroutineScope = CoroutineScope(Dispatchers.Main)
    private val isPlayingStateFlow = MutableStateFlow(false)

    actual fun play() {
        isPlayingStateFlow.value = true
        mediaPlayer.start()
    }

    actual fun pause() {
        isPlayingStateFlow.value = false
        mediaPlayer.pause()
    }

    actual val isPlaying: Boolean
        get() = mediaPlayer.isPlaying

    actual fun setRate(rate: Float) {
        val wasPaused = !mediaPlayer.isPlaying
        mediaPlayer.playbackParams = mediaPlayer.playbackParams.apply {
            speed = rate
        }
        if (wasPaused) {
            mediaPlayer.pause()
        }
    }

    actual fun setTime(millis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.seekTo(millis, MediaPlayer.SEEK_CLOSEST_SYNC)
        } else {
            // todo
        }
    }
    actual fun setTimeAccurate(millis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.seekTo(millis, MediaPlayer.SEEK_CLOSEST)
        } else {
            // todo
        }
    }

    actual fun getTimeMillis(): Long {
        return mediaPlayer.currentPosition.toLong()
    }

    actual fun getLengthMillis(): Long {
        return mediaPlayer.duration.toLong()
    }

    actual fun addOnTimeChangedListener(listener: OnTimeChangedListener) {
        mediaPlayerCoroutineScope.launch {
            isPlayingStateFlow.collectLatest {
                if (it) {
                    while (true) {
                        delay(33)
                        listener.onTimeChanged(mediaPlayer.currentPosition.toLong())
                    }
                }
            }
        }
    }

    actual fun dispose() {
        mediaPlayerCoroutineScope.cancel()
    }
}
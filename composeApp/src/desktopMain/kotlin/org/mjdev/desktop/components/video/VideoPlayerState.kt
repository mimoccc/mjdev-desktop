package org.mjdev.desktop.components.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class VideoPlayerState {
    private var mediaPlayer: MediaPlayer? = null
    private val deferredEffects = mutableListOf<(MediaPlayer) -> Unit>()

    actual fun doWithMediaPlayer(block: (MediaPlayer) -> Unit) {
        mediaPlayer?.let {
            block(it)
        } ?: run {
            deferredEffects.add(block)
        }
    }

    internal fun onMediaPlayerReady(mediaPlayer: uk.co.caprica.vlcj.player.base.MediaPlayer) {
        val mp = MediaPlayer(mediaPlayer)
        this.mediaPlayer = mp
        deferredEffects.forEach { block ->
            block(mp)
        }
        deferredEffects.clear()
    }
}

@Composable
actual fun rememberVideoPlayerState() = remember {
    VideoPlayerState()
}


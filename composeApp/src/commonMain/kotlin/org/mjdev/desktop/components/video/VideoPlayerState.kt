package org.mjdev.desktop.components.video

import androidx.compose.runtime.Composable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class VideoPlayerState {
    fun doWithMediaPlayer(block: (MediaPlayer) -> Unit)
}

@Composable
expect fun rememberVideoPlayerState(): VideoPlayerState
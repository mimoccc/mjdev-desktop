package org.mjdev.desktop.components.video

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.log.Log
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters

// todo performance
@Composable
actual fun VideoPlayer(
    mrl: String,
    videoInfo: VideoInfo,
    state: VideoPlayerState,
    modifier: Modifier,
) = withDesktopContext {
    var imageBitmap by remember(mrl) { mutableStateOf<ImageBitmap?>(null) }
    val renderCallback = remember(mrl) { RenderCallback { img -> imageBitmap = img } }
    val callbackVideoSurface = remember(mrl) {
        CallbackVideoSurface(
            CallbackVideoSurface(renderCallback),
            renderCallback,
            true,
            VideoSurfaceAdapters.getVideoSurfaceAdapter(),
        )
    }
    val mediaPlayer: EmbeddedMediaPlayer? = remember(mrl) {
        runCatching {
            MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer().apply {
                videoSurface().set(callbackVideoSurface)
            }
        }.onFailure { e ->
            Log.e(e)
        }.getOrNull()
    }
    val eventsHandler = remember(mrl) { MediaPlayerEvents(mediaPlayer) }
    if (imageBitmap != null) Image(
        modifier = modifier.background(Color.Black),
        bitmap = imageBitmap!!,
        contentDescription = null
    )
    DisposableEffect(mrl) {
        mediaPlayer?.audio()?.mute()
        mediaPlayer?.media()?.play(mrl)
        mediaPlayer?.events()?.addMediaPlayerEventListener(eventsHandler)
        onDispose {
            mediaPlayer?.release()
        }
    }
}

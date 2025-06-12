package org.mjdev.desktop.components.video

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.MediaPlayer.SEEK_CLOSEST
import android.os.Build
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext

@Composable
actual fun VideoPlayer(
    mrl: String,
    videoInfo: VideoInfo,
    state: VideoPlayerState,
    modifier: Modifier,
) = withDesktopContext {
    val time = rememberSaveable(mrl) { mutableIntStateOf(-1) }
    var videoRatio by rememberSaveable(mrl) { mutableFloatStateOf(-1f) }
    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                TextureView(context).also { it ->
                    it.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        lateinit var mediaPlayer: MediaPlayer
                        override fun onSurfaceTextureAvailable(
                            surfaceTexture: SurfaceTexture,
                            p1: Int,
                            p2: Int
                        ) {
                            val surface = Surface(surfaceTexture)
                            try {
                                mediaPlayer = MediaPlayer()
                                mediaPlayer.setDataSource(context, mrl.toUri())
                                mediaPlayer.setSurface(surface)
                                mediaPlayer.prepareAsync()
                                mediaPlayer.setOnPreparedListener { mediaPlayer ->
                                    if (time.intValue != -1) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            mediaPlayer.seekTo(time.intValue.toLong(), SEEK_CLOSEST)
                                        } else {
                                            // todo
                                        }
                                    }
                                    videoRatio =
                                        mediaPlayer.videoWidth.toFloat() / mediaPlayer.videoHeight
//                                val containerRatio =
//                                    binding.constraintContainer.width.toDouble() / binding.constraintContainer.height.toDouble()
//                                val videoRatio = mediaPlayer.videoWidth.toDouble() / mediaPlayer.videoHeight.toDouble()
//
//                                val constraintLayoutParams = binding.textureView.layoutParams as ConstraintLayout.LayoutParams
//                                if (containerRatio > videoRatio) {
//                                    constraintLayoutParams.width = 0
//                                    constraintLayoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
//                                    constraintLayoutParams.dimensionRatio =
//                                        "W,${mediaPlayer.videoWidth}:${mediaPlayer.videoHeight}"
//                                } else {
//                                    constraintLayoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT
//                                    constraintLayoutParams.height = 0
//                                    constraintLayoutParams.dimensionRatio =
//                                        "H,${mediaPlayer.videoWidth}:${mediaPlayer.videoHeight}"
//                                }
//                                binding.textureView.layoutParams = constraintLayoutParams
//                                initViewModel(projectItem, mediaPlayer.duration.toLong(), mediaPlayer)
                                    state.onMediaPlayerReady(mediaPlayer)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        override fun onSurfaceTextureSizeChanged(
                            p0: SurfaceTexture,
                            p1: Int,
                            p2: Int
                        ) {}
                        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                            time.value = mediaPlayer.currentPosition
                            state.doWithMediaPlayer { mp ->
                                mp.dispose()
                            }
                            return false
                        }
                        override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier.let {
                if (videoRatio != -1f) {
                    it.aspectRatio(videoRatio).align(Alignment.Center)
                } else {
                    it
                }
            }
        )
    }
}
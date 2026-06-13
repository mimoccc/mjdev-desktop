package org.mjdev.desktop.components.video

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.log.Log

@Composable
fun VideoView(
    modifier: Modifier = Modifier,
    src: Any? = null,
    alignment: Alignment = Alignment.Center,
    alpha: Float = 1f,
    contentDescription: String? = "",
    colorFilter: ColorFilter? = null,
    contentScale: ContentScale = ContentScale.Crop,
    state: VideoPlayerState = rememberVideoPlayerState(),
    onFail: (Throwable) -> Unit = { e -> Log.e(e) },
    onLoading: () -> Unit = {},
    onVideoFinish: () -> Unit = {},
    onLoaded: (duration: Long) -> Unit = {},
) = withDesktopContext {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = alignment,
    ) {
        val url =
            remember(src) {
                src.toString().let { s ->
                    if (s.startsWith("http://") || s.startsWith("https://")) {
                        s
                    } else {
                        "file://$s"
                    }
                }
            }
        VideoPlayer(
            modifier = Modifier.fillMaxSize(),
            mrl = url,
            videoInfo =
                VideoInfo(
                    videoWidth = maxWidth.value.toInt(),
                    videoHeight = maxHeight.value.toInt(),
                ),
            state = state,
        )
    }
}

@Preview
@Composable
fun PreviewVideoView() =
    preview {
        VideoView()
    }

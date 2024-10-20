package eu.mjdev.desktop.components.video

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import eu.mjdev.desktop.components.video.base.VideoPlayerFFMpeg
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.log.Log
import java.io.File

// todo
@Suppress("UNUSED_PARAMETER", "FunctionName")
@Preview
@Composable
fun VideoView(
    modifier: Modifier = Modifier,
    src: Any? = null,
    alignment: Alignment = Alignment.Center,
    alpha: Float = 1f,
    contentDescription: String? = "",
    colorFilter: ColorFilter? = null,
    contentScale: ContentScale = ContentScale.Crop,
    onFail: (Throwable) -> Unit = { e -> Log.e(e) }
) = Box(
    modifier = modifier
) {
    // todo
    VideoPlayerFFMpeg(
        modifier = Modifier.fillMaxSize().align(alignment),
        file = when (src) {
            is String -> src
            is File -> src.absolutePath
            else -> src.toString()
        }
    )
}

// todo
@Preview
@Composable
fun VideoViewPreview() = preview {
    VideoView()
}

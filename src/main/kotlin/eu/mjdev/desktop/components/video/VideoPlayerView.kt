package eu.mjdev.desktop.components.video

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import java.io.File

@Suppress("UNUSED_PARAMETER")
@Composable
fun VideoPlayerView(
    modifier: Modifier = Modifier,
    src: Any?,
    alignment: Alignment = Alignment.Center,
    alpha: Float = 1f,
    contentDescription: String? = "",
    colorFilter: ColorFilter? = null,
    contentScale: ContentScale = ContentScale.Crop,
    onFail: (Throwable) -> Unit = { e -> e.printStackTrace() }
) = Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
) {
    val mediaUrl = when (src) {
        is String -> src
        is File -> src.absolutePath
        else -> src.toString()
    }
    VideoPlayerFFMpeg(
        modifier = Modifier.align(alignment),
        file = mediaUrl
    )
}

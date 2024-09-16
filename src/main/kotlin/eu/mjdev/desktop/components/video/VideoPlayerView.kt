package eu.mjdev.desktop.components.video

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale

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
    modifier = modifier
) {
    // todo
//    VideoPlayerFFMpeg(
//        modifier = Modifier.fillMaxSize().align(alignment),
//        file = when (src) {
//            is String -> src
//            is File -> src.absolutePath
//            else -> src.toString()
//        }
//    )
}

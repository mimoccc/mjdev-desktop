package eu.mjdev.desktop.components.image

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import eu.mjdev.desktop.components.video.VideoView
import eu.mjdev.desktop.extensions.Compose.asyncImageLoader
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import java.io.File
import androidx.compose.foundation.Image as ComposeImage

@Suppress("FunctionName")
@Composable
fun ImageAny(
    src: Any?,
    api: DesktopProvider = LocalDesktop.current,
    contentDescription: String? = "",
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    onLoading: () -> Unit = {},
    onFail: (error: Throwable) -> Unit = {}
) {
    // todo from mime
    val isGif = src.toString().trim().endsWith(".gif")
    val isVideo = src.toString().trim().let {
        it.endsWith(".mp4") || it.endsWith(".mpg")
    }
    when {
        src == null -> Box(
            modifier = modifier
        )

        isGif -> GifView(
            modifier = modifier,
            src = when (src) {
                is File -> src.absolutePath
                is String -> src
                else -> src.toString()
            }
        )

        isVideo -> VideoView(
            modifier = modifier,
            src = src,
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        src is ImageVector -> ComposeImage(
            modifier = modifier,
            imageVector = src,
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        src is Painter -> ComposeImage(
            modifier = modifier,
            painter = src,
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        else -> AsyncImage(
            modifier = modifier,
            model = src,
            contentDescription = contentDescription,
            contentScale = contentScale,
            filterQuality = filterQuality,
            imageLoader = api.imageLoader ?: asyncImageLoader(),
            onLoading = {
                onLoading()
            },
            onError = {
                onFail(it.result.throwable)
            },
        )
    }
}

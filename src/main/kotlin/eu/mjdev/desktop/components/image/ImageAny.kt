package eu.mjdev.desktop.components.image

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import eu.mjdev.desktop.components.video.VideoView
import eu.mjdev.desktop.extensions.Compose.asyncImageLoader
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import org.jetbrains.skia.Bitmap
import java.io.File
import androidx.compose.foundation.Image as ComposeImage

@Suppress("FunctionName", "UNUSED_PARAMETER")
@Composable
fun ImageAny(
    src: Any? = null,
    contentDescription: String? = "",
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    placeholder: @Composable () -> Unit = {}, // todo
    onLoading: () -> Unit = {},
    onFail: (error: Throwable) -> Unit = {}
) = withDesktopScope {
    // todo from mime
    val isGif = src.toString().trim().endsWith(".gif")
    // todo from mime
    val isVideo = src.toString().trim().let {
        it.endsWith(".mp4") || it.endsWith(".mpg")
    }
    when {
        src == null -> ComposeImage(
            modifier = modifier,
            imageVector = Icons.Default.BrokenImage,
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        src is Color -> Box(
            modifier = modifier.background(src)
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

        src is Bitmap -> ComposeImage(
            modifier = modifier,
            bitmap = src.asComposeImageBitmap(),
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        src is ImageBitmap -> ComposeImage(
            modifier = modifier,
            bitmap = src,
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

        isGif -> GifView(
            modifier = modifier,
            src = when {
                src is File -> src.absolutePath
                else -> src.toString()
            }
        )

        isVideo -> VideoView(
            modifier = modifier,
            src = when {
                src is File -> src.absolutePath
                else -> src.toString()
            },
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

@Preview
@Composable
fun ImageAnyPreview() = preview {
    ImageAny()
}

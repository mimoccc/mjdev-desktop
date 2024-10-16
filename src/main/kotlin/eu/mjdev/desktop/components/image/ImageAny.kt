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
import eu.mjdev.desktop.extensions.Custom.flowBlock
import eu.mjdev.desktop.helpers.internal.ImagesProvider
import eu.mjdev.desktop.helpers.internal.ImagesProvider.Companion.getOne
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
    val image = flowBlock(null) {
        (src as? ImagesProvider).let { provider -> provider?.getOne() ?: src }
    }
    // todo from mime
    val isGif = image.value.toString().trim().endsWith(".gif")
    // todo from mime
    val isVideo = image.value.toString().trim().let {
        it.endsWith(".mp4") || it.endsWith(".mpg")
    }
    when {
        image.value == null -> ComposeImage(
            modifier = modifier,
            imageVector = Icons.Default.BrokenImage,
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        image.value is Color -> Box(
            modifier = modifier.background(image.value as Color)
        )

        image.value is Painter -> ComposeImage(
            modifier = modifier,
            painter = image.value as Painter,
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        image.value is Bitmap -> ComposeImage(
            modifier = modifier,
            bitmap = (image.value as Bitmap).asComposeImageBitmap(),
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        image.value is ImageBitmap -> ComposeImage(
            modifier = modifier,
            bitmap = image.value as ImageBitmap,
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        image.value is ImageVector -> ComposeImage(
            modifier = modifier,
            imageVector = image.value as ImageVector,
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        isGif -> GifView(
            modifier = modifier,
            src = when {
                image.value is File -> (image.value as File).absolutePath
                else -> image.value.toString()
            }
        )

        isVideo -> VideoView(
            modifier = modifier,
            src = when {
                image.value is File -> (image.value as File).absolutePath
                else -> image.value.toString()
            },
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        else -> AsyncImage(
            modifier = modifier,
            model = image.value,
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

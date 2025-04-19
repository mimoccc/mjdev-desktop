package org.mjdev.desktop.components.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.compose.AsyncImage
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.helpers.image.ImageLoader.asyncImageLoader
import org.mjdev.desktop.icons.image.BrokenImage
import androidx.compose.foundation.Image as ComposeImage
import org.jetbrains.compose.ui.tooling.preview.Preview

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
    // todo
    imageLoader:ImageLoader? = asyncImageLoader(),
    placeholder: @Composable () -> Unit = {}, // todo
    onLoading: () -> Unit = {},
    onFail: (error: Throwable) -> Unit = {},
    onAnimationFinish: () -> Unit = {}
)  {
    // todo from mime
    val isGif = src.toString().trim().endsWith(".gif")
    // todo from mime
    val isVideo = src.toString().trim().let {
        it.endsWith(".mp4") || it.endsWith(".mpg")
    }
    when {
        src == null -> ComposeImage(
            modifier = modifier,
            imageVector = BrokenImage,
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

//        src is Bitmap -> ComposeImage(
//            modifier = modifier,
//            bitmap = src.asComposeImageBitmap(),
//            alignment = alignment,
//            alpha = alpha,
//            contentDescription = contentDescription,
//            colorFilter = colorFilter,
//            contentScale = contentScale
//        )

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

//        isGif -> GifView(
//            modifier = modifier,
//            src = when {
//                src is File -> src.absolutePath
//                else -> src.toString()
//            },
//            onAnimationFinish = onAnimationFinish
//        )

//        isVideo -> VideoView(
//            modifier = modifier,
//            src = when {
//                src is File -> src.absolutePath
//                else -> src.toString()
//            },
//            alignment = alignment,
//            alpha = alpha,
//            contentDescription = contentDescription,
//            colorFilter = colorFilter,
//            contentScale = contentScale,
//            onVideoFinish = onAnimationFinish
//        )

        else -> AsyncImage(
            modifier = modifier,
            model = src,
            contentDescription = contentDescription,
            contentScale = contentScale,
            filterQuality = filterQuality,
            imageLoader = imageLoader ?: asyncImageLoader(),
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

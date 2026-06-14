/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

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
import okio.Path
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.video.VideoView
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Compose.runAsync
import org.mjdev.desktop.extensions.MimeTypeExt.isGif
import org.mjdev.desktop.extensions.MimeTypeExt.isVideo
import org.mjdev.desktop.extensions.PathExt.absolutePath
import org.mjdev.desktop.helpers.image.ImageLoader.asyncImageLoader
import org.mjdev.desktop.icons.image.BrokenImage
import androidx.compose.foundation.Image as ComposeImage

@Suppress("FunctionName", "UNUSED_PARAMETER", "ModifierParameter")
@Composable
fun ImageAny(
    src: Any? = null,
    modifier: Modifier = Modifier,
    contentDescription: String? = "",
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    imageLoader: ImageLoader? = asyncImageLoader(), // todo?
    placeholder: @Composable () -> Unit = {}, // todo
    onLoading: () -> Unit = {},
    onLoaded: (duration: Long) -> Unit = {},
    onFail: (error: Throwable) -> Unit = {},
    onAnimationFinish: () -> Unit = {},
) {
    when {
        src == null -> {
            runAsync { onLoaded(0L) }
            ComposeImage(
                modifier = modifier,
                imageVector = BrokenImage,
                alignment = alignment,
                alpha = alpha,
                contentDescription = contentDescription,
                colorFilter = colorFilter,
                contentScale = contentScale,
            )
        }

        src is Color -> {
            runAsync { onLoaded(0L) }
            Box(
                modifier = modifier.background(src),
            )
        }

        src is Painter -> {
            runAsync { onLoaded(0L) }
            ComposeImage(
                modifier = modifier,
                painter = src,
                alignment = alignment,
                alpha = alpha,
                contentDescription = contentDescription,
                colorFilter = colorFilter,
                contentScale = contentScale,
            )
        }
//        src is Bitmap -> ComposeImage(
//            modifier = modifier,
//            bitmap = src.asComposeImageBitmap(),
//            alignment = alignment,
//            alpha = alpha,
//            contentDescription = contentDescription,
//            colorFilter = colorFilter,
//            contentScale = contentScale
//        )

        src is ImageBitmap -> {
            runAsync { onLoaded(0L) }
            ComposeImage(
                modifier = modifier,
                bitmap = src,
                alignment = alignment,
                alpha = alpha,
                contentDescription = contentDescription,
                colorFilter = colorFilter,
                contentScale = contentScale,
            )
        }

        src is ImageVector -> {
            runAsync { onLoaded(0L) }
            ComposeImage(
                modifier = modifier,
                imageVector = src,
                alignment = alignment,
                alpha = alpha,
                contentDescription = contentDescription,
                colorFilter = colorFilter,
                contentScale = contentScale,
            )
        }

        src.isGif ->
            GifView(
                modifier = modifier,
                src =
                    when {
                        src is Path -> src.absolutePath
                        else -> src.toString()
                    },
                onLoading = onLoading,
                onLoaded = onLoaded,
                onAnimationFinish = onAnimationFinish,
                onFail = onFail,
            )

        src.isVideo ->
            VideoView(
                modifier = modifier,
                src =
                    when {
                        src is Path -> src.absolutePath
                        else -> src.toString()
                    },
                onLoading = { onLoading() },
                onLoaded = onLoaded,
                onFail = onFail,
            )

        else ->
            AsyncImage(
                modifier = modifier,
                model = src,
                contentDescription = contentDescription,
                contentScale = contentScale,
                filterQuality = filterQuality,
                imageLoader = imageLoader ?: asyncImageLoader(),
                onLoading = { onLoading() },
                onSuccess = { onLoaded(0L) },
                onError = { e -> onFail(e.result.throwable) },
            )
    }
}

@Preview
@Composable
fun PreviewImageAny() =
    preview {
        ImageAny()
    }

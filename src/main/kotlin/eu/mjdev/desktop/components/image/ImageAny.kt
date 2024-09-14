package eu.mjdev.desktop.components.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.foundation.Image as ComposeImage

@Suppress("FunctionName")
@Composable
fun ImageAny(
    src: Any?,
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
    when {
        src is ImageVector -> ComposeImage(
            modifier = modifier,
            imageVector = src,
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
            onError = {
                onFail(it.result.throwable)
            },
            onLoading = {
                onLoading()
            },
        )
    }
}



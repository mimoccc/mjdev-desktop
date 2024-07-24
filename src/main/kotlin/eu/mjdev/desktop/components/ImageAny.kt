package eu.mjdev.desktop.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Image.loadPicture
import androidx.compose.foundation.Image as ComposeImage

@Suppress("FunctionName")
@Composable
fun ImageAny(
    src: Any?,
    showLoading: Boolean = false,
    contentDescription: String? = "",
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    onFail: (error: Throwable) -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Throwable?>(null) }
    var imageBitmap by remember { mutableStateOf(if (src is ImageBitmap) src else null) }
    when {
        (isLoading && showLoading) -> Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }

        src is ImageVector -> ComposeImage(
            modifier = modifier,
            imageVector = src,
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale
        )

        imageBitmap != null -> ComposeImage(
            modifier = modifier,
            bitmap = imageBitmap!!,
            alignment = alignment,
            alpha = alpha,
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            filterQuality = filterQuality,
            contentScale = contentScale
        )
    }
    launchedEffect(src) {
        isLoading = true
        loadPicture(src)
            .onSuccess {
                imageBitmap = it
                isLoading = false
            }
            .onFailure { e ->
                error = e
                isLoading = false
            }
    }
    launchedEffect(error) {
        if (error != null) onFail(error!!)
    }
}

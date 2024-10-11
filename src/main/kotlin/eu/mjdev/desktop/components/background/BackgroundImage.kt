package eu.mjdev.desktop.components.background

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.extensions.Compose.Crossfade
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.helpers.internal.Queue
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("FunctionName")
@Composable
fun BackgroundImage(
    modifier: Modifier = Modifier,
    fadeInDuration: Long = 3000L,
    fadeOutDuration: Long = 5000L,
    onError: (Throwable) -> Unit = { e -> e.printStackTrace() },
    onChange: (bck: Any?) -> Unit = {}
) = withDesktopScope {
    val switchDelay = theme.backgroundRotationDelay
    val backgroundQueue = remember(backgrounds, backgrounds.size) { Queue(backgrounds) }
    var currentBackground by remember { mutableStateOf(backgroundQueue.nextOrNull()) }
    Box(
        modifier = modifier.background(backgroundColor)
    ) {
        Crossfade(
            targetState = currentBackground ?: Color.SuperDarkGray,
            fadeInDuration = fadeInDuration,
            fadeOutDuration = fadeOutDuration
        ) { value ->
            scope.launch(Dispatchers.IO) {
                onChange(currentBackground)
            }
            ImageAny(
                modifier = modifier,
                contentScale = ContentScale.Crop,
                src = value,
                onFail = onError
            )
        }
        launchedEffect(currentBackground) {
            if (switchDelay > 0) {
                delay(switchDelay)
                currentBackground = backgroundQueue.nextOrNull()
            }
        }
    }
}

@Preview
@Composable
fun BackgroundImagePreview() = BackgroundImage()

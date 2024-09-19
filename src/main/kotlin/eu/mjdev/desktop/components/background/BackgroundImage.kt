package eu.mjdev.desktop.components.background

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.helpers.internal.Queue.Companion.mutableQueue
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview
@Composable
fun BackgroundImage(
    modifier: Modifier = Modifier,
    api: DesktopProvider = LocalDesktop.current,
    fadeInDuration: Long = 3000L,
    fadeOutDuration: Long = 5000L,
    onError: (Throwable) -> Unit = { e -> e.printStackTrace() },
    onChange: (bck: Any?) -> Unit = {}
) {
    val switchDelay by remember { api.currentUser.theme.backgroundRotationDelayState }
    val backgroundColor by remember { api.currentUser.theme.backgroundColorState }
    val userBackgrounds by remember { api.currentUser.config.desktopBackgroundsState }
    val backgrounds by rememberCalculated { api.appsProvider.backgrounds + userBackgrounds }
    val backgroundQueue = remember { mutableQueue(backgrounds) }
    var currentBackground: Any? by remember { mutableStateOf(backgroundQueue.value) }
    Box(
        modifier = modifier.background(backgroundColor)
    ) {
        AnimatedContent(
            currentBackground,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = fadeInDuration.toInt())) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = fadeOutDuration.toInt()))
            }
        ) { value ->
            api.scope.launch {
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
                currentBackground = backgroundQueue.value
            }
        }
    }
}

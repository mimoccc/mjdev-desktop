package eu.mjdev.desktop.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.helpers.Queue.Companion.mutableQueue
import kotlinx.coroutines.delay

@Preview
@Composable
fun BackgroundImage(
    modifier: Modifier = Modifier,
    switchDelay: Long = 5000L,
    fadeInDuration: Long = 3000L,
    fadeOutDuration: Long = 5000L,
    backgroundColor: Color = Color.Transparent,
    backgrounds: List<Any> = emptyList(),
    onChange: (bck: Any?) -> Unit = {}
) = Box(
    modifier = modifier.background(backgroundColor)
) {
    val backgroundQueue = remember { mutableQueue(backgrounds) }
    val currentBackground = remember { mutableStateOf(backgroundQueue.value) }
    AnimatedContent(
        backgroundQueue.value,
        transitionSpec = {
            fadeIn(animationSpec = tween(durationMillis = fadeInDuration.toInt())) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = fadeOutDuration.toInt()))
        }
    ) { value ->
        ImageAny(
            modifier = modifier,
            contentScale = ContentScale.Crop,
            src = value
        )
    }
    launchedEffect(currentBackground.value) {
        delay(switchDelay)
        currentBackground.value = backgroundQueue.value
        onChange(currentBackground.value)
    }
}

package org.mjdev.desktop.components.background

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.mjdev.desktop.components.image.ImageAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.ContrastColorFilter
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.Crossfade
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.LaunchedEffect.launch
import org.mjdev.desktop.extensions.LaunchedEffect.launchedEffect
import org.mjdev.desktop.extensions.MutableStateExt.rememberComputed
import org.mjdev.desktop.helpers.generic.Queue
import kotlin.math.min
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("FunctionName")
@Composable
fun BackgroundImage(
    modifier: Modifier = Modifier,
    switchDelay: Long = 60000L,
    backgroundColor: Color = Color.SuperDarkGray,
    images: MutableList<Any> = mutableListOf(),
    onError: (Throwable) -> Unit = { e -> e.printStackTrace() },
    onChange: (bck: Any?) -> Unit = {}
) = withDesktopContext {
    Box(
        modifier = modifier.background(backgroundColor)
    ) {
        val backgroundQueue by rememberComputed(images, images.size) { Queue(images) }
        val fadeInDuration by rememberComputed(switchDelay) { switchDelay / 8 }
        val fadeOutDuration by rememberComputed(switchDelay) { switchDelay / 4 }
        var currentBackground by remember { mutableStateOf(backgroundQueue.nextOrNull()) }
        Crossfade(
            targetState = currentBackground ?: Color.SuperDarkGray,
            fadeInDuration = fadeInDuration,
            fadeOutDuration = fadeOutDuration
        ) { imageValue ->
            launch(Dispatchers.Default) {
                onChange(currentBackground)
            }
            ImageAny(
                modifier = modifier,
                contentScale = ContentScale.Crop,
                imageLoader = imageLoader,
                src = imageValue,
                onFail = onError,
                colorFilter = ContrastColorFilter(1.5f),
                onAnimationFinish = {
                    currentBackground = backgroundQueue.nextOrNull()
                }
            )
            Box(
                modifier = modifier.background(
                    Brush.radialGradient(
                        colors = listOf(
                            Transparent,
                            Transparent,
                            Transparent,
                            Transparent,
                            backgroundColor.alpha(0.1f),
                            backgroundColor.alpha(0.3f),
                            backgroundColor
                        ),
                        center = Offset(
                            containerSize.width.value / 2,
                            containerSize.height.value / 2
                        ),
                        radius = min(
                            containerSize.width.value,
                            containerSize.height.value
                        ) * 1.2f
                    )
                )
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

@Suppress("unused")
@Preview
@Composable
fun BackgroundImagePreview() = preview {
    BackgroundImage()
}

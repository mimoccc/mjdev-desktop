package org.mjdev.desktop.components.background

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import org.mjdev.desktop.components.image.ImageAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.ContrastColorFilter
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.Crossfade
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.MutableStateExt.rememberComputed
import org.mjdev.desktop.helpers.generic.Queue
import kotlin.math.min
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.log.Log

@Suppress("FunctionName", "CoroutineCreationDuringComposition")
@Composable
fun BackgroundImage(
    modifier: Modifier = Modifier,
    switchDelay: Long = 60000L,
    fadeInDuration: Long = 60000L / 8,
    fadeOutDuration: Long = 60000L / 4,
    backgroundColor: Color = Color.SuperDarkGray,
    images: MutableList<Any> = mutableListOf(),
    onError: (Throwable) -> Unit = { e -> Log.e(e) },
    onChange: (bck: Any?) -> Unit = {}
) = withDesktopContext {
    Box(
        modifier = modifier.background(backgroundColor)
    ) {
        val imagesSize by rememberComputed(images.size) { images.size }
        val backgroundQueue by rememberComputed(imagesSize) { Queue(images) }
        var currentBackground: Any by remember { mutableStateOf(backgroundColor) }
        Crossfade(
            targetState = currentBackground,
            fadeInDuration = fadeInDuration,
            fadeOutDuration = fadeOutDuration
        ) { imageValue ->
            ImageAny(
                modifier = modifier,
                contentScale = ContentScale.Crop,
                imageLoader = imageLoader,
                src = imageValue,
                onFail = onError,
                colorFilter = ContrastColorFilter(1.5f),
                onLoading = {
                    Log.d("Background image loading: $imageValue")
                },
                onLoaded = { duration ->
                    Log.d("Background image loaded: $imageValue, duration: $duration")
                },
                onAnimationFinish = {
                    Log.d("Background image finished: $imageValue")
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
        LaunchedEffect(imagesSize) {
            currentBackground = backgroundQueue.nextOrNull() ?: backgroundColor
        }
        // todo duration from movie or gif
        LaunchedEffect(currentBackground) {
            delay(switchDelay)
            currentBackground = backgroundQueue.nextOrNull() ?: backgroundColor
        }
        LaunchedEffect(currentBackground) {
            onChange(currentBackground)
        }
    }
}

@Suppress("unused")
@Preview
@Composable
fun PreviewBackgroundImage() = preview {
    BackgroundImage()
}

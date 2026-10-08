package org.psippr.desktop.components.background

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
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import org.psippr.desktop.components.image.ImageAny
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Colors.ContrastColorFilter
import org.psippr.desktop.extensions.Colors.SuperDarkGray
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.extensions.Compose.Crossfade
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.helpers.generic.Queue
import org.psippr.desktop.log.Log
import kotlin.math.min

@Suppress("FunctionName", "CoroutineCreationDuringComposition")
@Composable
fun BackgroundImage(
    modifier: Modifier = Modifier,
    switchDelay: Long = BackgroundImageDefaults.SWITCH_DELAY_MS,
    fadeInDuration: Long = BackgroundImageDefaults.FADE_IN_DURATION_MS,
    fadeOutDuration: Long = BackgroundImageDefaults.FADE_OUT_DURATION_MS,
    backgroundColor: Color = Color.SuperDarkGray,
    images: List<Any> = listOf(),
    onError: (Throwable) -> Unit = { e -> Log.e(e) },
    onChange: (bck: Any?) -> Unit = {},
) = withDesktopContext {
    Box(
        modifier = modifier.background(backgroundColor),
    ) {
        // keyed by the list itself (not only its size), so any new wallpaper list restarts the queue
        val backgroundQueue = remember(images) { Queue(images) }
        var currentBackground: Any by remember { mutableStateOf(backgroundColor) }
        Crossfade(
            targetState = currentBackground,
            fadeInDuration = fadeInDuration,
            fadeOutDuration = fadeOutDuration,
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
                },
            )
            Box(
                modifier =
                    modifier.background(
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Transparent,
                                    Transparent,
                                    Transparent,
                                    Transparent,
                                    backgroundColor.alpha(0.1f),
                                    backgroundColor.alpha(0.3f),
                                    backgroundColor,
                                ),
                            center =
                                Offset(
                                    containerSize.width.value / 2,
                                    containerSize.height.value / 2,
                                ),
                            radius =
                                min(
                                    containerSize.width.value,
                                    containerSize.height.value,
                                ) * 1.2f,
                        ),
                    ),
            )
        }
        // a new list (e.g. loaded after the first frame) shows its first wallpaper right away,
        // which also restarts the rotation below when it was stuck on the fallback color
        LaunchedEffect(images) {
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

package org.mjdev.desktop.components.image

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.helpers.gif.GifDecoder
import org.mjdev.desktop.components.image.GifViewState.Companion.rememberGifViewState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.context.LocalDesktopContext
import org.mjdev.desktop.log.Log
import kotlin.math.absoluteValue

@Suppress("FunctionName")
@Composable
fun GifView(
    modifier: Modifier = Modifier,
    src: String = "",
    state: GifViewState = rememberGifViewState(src),
    onAnimationFinish: () -> Unit = {},
    onLoading: () -> Unit = { Log.d("Loading GIF: ${state.src}") },
    onLoaded: (duration: Long) -> Unit = { Log.d("Loading GIF: ${state.src} is loaded.") },
    onFail: (error: Throwable) -> Unit = { e -> Log.e("Failed to load GIF: ${state.src}", e) }
) = withDesktopContext {
    BoxWithConstraints {
        Canvas(modifier = modifier) {
            drawImage(
                image = state.currentImage ?: ImageBitmap(1, 1),
                srcSize = IntSize(state.currentImage?.width ?: 0, state.currentImage?.height ?: 0),
                dstSize = IntSize(constraints.maxWidth, constraints.maxHeight),
            )
        }
        LaunchedEffect(state.currentFrame) {
//            if(state.isLoading) {
//                onLoading()
//            }
            if (state.currentFrame == 0) {
                onLoaded(state.duration)
            }
            if (state.currentFrame > -1) {
                delay(state.currentFrameDelay)
            }
            if (state.hasNextFrame) {
                state.nextFrame()
            } else {
                state.reset()
            }
        }
    }
}

@Suppress("RedundantSuspendModifier", "unused", "MemberVisibilityCanBePrivate")
class GifViewState {
    var src: String = ""
        private set
    var gifDecoder: GifDecoder? = null

    var isLoading by mutableStateOf(true)
        internal set
    var currentFrame by mutableIntStateOf(-1)
        internal set
    val currentImage: ImageBitmap?
        get() = gifDecoder?.getFrame(currentFrame)
    val currentFrameDelay: Long
        get() = gifDecoder?.getDelay(currentFrame) ?: 0
    val framesCount
        get() = gifDecoder?.getFrameCount() ?: 0
    val hasNextFrame: Boolean
        get() = currentFrame < framesCount - 1
    val duration: Long
        get() = gifDecoder?.getDuration() ?: 0

    suspend fun nextFrame() {
        if (isLoading || framesCount == 0) return
        var nextFrame = currentFrame + 1
        if (nextFrame >= framesCount) nextFrame = 0
        currentFrame = nextFrame
    }

    suspend fun prevFrame() {
        if (isLoading || framesCount == 0) return
        var prevFrame = currentFrame - 1
        if (prevFrame < 0) prevFrame = 0
        currentFrame = prevFrame
    }

    suspend fun reset() {
        if (isLoading || framesCount == 0) return
        currentFrame = 0
    }

    suspend fun load(src: String) {
        isLoading = true
        this.src = src
        gifDecoder = GifDecoder().apply { from(src) }
        while (gifDecoder!!.getFrameCount() == 0) {
            delay(100L)
        }
        isLoading = false
        currentFrame = 0
    }

    companion object {
        @Composable
        fun rememberGifViewState(
            src: String,
            context: IDesktopContext = LocalDesktopContext.current,
            scope: CoroutineScope = context.scope
        ): GifViewState = remember(src) {
            GifViewState().apply {
                scope.launch {
                    load(src)
                }
            }
        }
    }
}

@Suppress("unused")
@Preview
@Composable
fun PreviewGifView() = preview {
    GifView()
}
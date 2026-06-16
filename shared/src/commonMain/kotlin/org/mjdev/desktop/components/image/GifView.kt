package org.mjdev.desktop.components.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.image.GifViewState.Companion.rememberGifViewState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.context.LocalDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.helpers.gif.GifDecoder
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
    onFail: (error: Throwable) -> Unit = { e -> Log.e("Failed to load GIF: ${state.src}", e) },
) = withDesktopContext {
    BoxWithConstraints {
        // Render the decoded frame via the standard Image composable (the same path AsyncImage
        // uses for bitmaps). The previous Canvas { drawImage(srcSize, dstSize) } stopped painting
        // anything after the Compose bump — frames decoded fine but never appeared (gray bg).
        val image = state.currentImage
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop,
            )
        }
        // single playback loop per gif: decodes the NEXT frame off the UI thread while the current
        // one is on screen (prefetch pipeline), so swaps are instant and we never spin the CPU.
        LaunchedEffect(state) {
            state.play(onLoaded = onLoaded, onAnimationFinish = onAnimationFinish)
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
    var currentImage by mutableStateOf<ImageBitmap?>(null)
        internal set
    val currentFrameDelay: Long
        get() = gifDecoder?.getDelay(currentFrame) ?: 0
    val framesCount
        get() = gifDecoder?.getFrameCount() ?: 0
    val hasNextFrame: Boolean
        get() = currentFrame < framesCount - 1
    val duration: Long
        get() = gifDecoder?.getDuration() ?: 0

    // Drives playback in a single coroutine: shows frame n while decoding n+1 in the background,
    // so the visible swap is instant and the CPU is never spun (sub-MIN_FRAME_DELAY_MS gifs are
    // clamped). Building frames off the UI thread keeps the desktop responsive.
    suspend fun play(
        onLoaded: (Long) -> Unit,
        onAnimationFinish: () -> Unit,
    ) = coroutineScope {
        while (isActive && (gifDecoder == null || isLoading || framesCount == 0)) {
            delay(50L)
        }
        val decoder = gifDecoder ?: return@coroutineScope
        val count = framesCount
        if (count == 0) return@coroutineScope
        var n = 0
        currentFrame = 0
        currentImage = withContext(Dispatchers.Default) { decoder.getFrame(0) }
        onLoaded(duration)
        if (count <= 1) return@coroutineScope
        while (isActive) {
            val next = if (n < count - 1) n + 1 else 0
            val prefetch = async(Dispatchers.Default) { decoder.getFrame(next) }
            delay(decoder.getDelay(n).coerceAtLeast(MIN_FRAME_DELAY_MS))
            val bitmap = prefetch.await()
            n = next
            currentFrame = n
            currentImage = bitmap
            if (n == 0) onAnimationFinish()
        }
    }

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
        // Clamp very short / zero frame delays so a "play as fast as possible" gif can't spin the
        // CPU (which starved the UI — control center wouldn't open). ~50fps ceiling.
        const val MIN_FRAME_DELAY_MS = 20L

        @Composable
        fun rememberGifViewState(
            src: String,
            context: IDesktopContext = LocalDesktopContext.current,
            scope: CoroutineScope = context.scope,
        ): GifViewState =
            remember(src) {
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
fun PreviewGifView() =
    preview {
        GifView()
    }

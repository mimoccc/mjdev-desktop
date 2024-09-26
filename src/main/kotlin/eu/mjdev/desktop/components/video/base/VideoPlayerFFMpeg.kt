package eu.mjdev.desktop.components.video.base

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlin.math.min

@Suppress("unused")
class FFmpegVideoPlayerState {
    private val kContext = KAVFormatContext()

    var time: Long = 0L
        internal set
    var progress: Float by mutableStateOf(0f)
        internal set
    @Suppress("RedundantSetter")
    private var aspectRatio: Float by mutableStateOf(1f)
        private set
    var displayFPS: Float by mutableStateOf(0f)
        internal set
    var decodedFPS: Float by mutableStateOf(0f)
        internal set

    var frameGrabber: KFrameGrabber? by mutableStateOf(null)
        private set

    @Suppress("MemberVisibilityCanBePrivate")
    var metadata: Map<String, String> by mutableStateOf(emptyMap())
        private set

    fun open(file: String) {
        kContext.openInput(file)
        metadata = kContext.findMetadata()
    }

    fun close() {
        kContext.closeInput()
    }

    fun streams(): List<KVideoStream> = kContext.findVideoStreams()
    fun codec(stream: KVideoStream): KAVCodec = kContext.findCodec(stream)

    fun play(
        stream: KVideoStream,
        hwDecoder: KHWDecoder? = null,
        targetSize: IntSize? = null
    ) {
        frameGrabber?.close() // close running frame grabber
        frameGrabber = KFrameGrabber(stream, kContext, hwDecoder, targetSize)
        aspectRatio = stream.width.toFloat() / stream.height.toFloat()
    }

    fun stop() {
        frameGrabber?.close()
        frameGrabber = null
    }

    internal var seekPosition: Float = -1f

    fun seek(position: Float) {
        seekPosition = position
    }

    fun togglePause() {
        //TODO
    }
}

@Composable
fun VideoPlayerFFMpeg(
    modifier: Modifier = Modifier,
    state: FFmpegVideoPlayerState = remember { FFmpegVideoPlayerState() },
    file: String
) {
    var frame by remember { mutableStateOf(0) }
    val videoImage = mutableStateOf<ImageBitmap?>(null)
    LaunchedEffect(file, Dispatchers.IO) {
        state.open(file)
        val stream = state.streams().first()
        val codec = state.codec(stream)
        println("Codec: ${codec.name}")
        codec.hwDecoder.forEach { println("   Decoder: ${it.name}") }
        val scale = 1
        val targetSize = IntSize(stream.width / scale, stream.height / scale)
        state.play(stream, codec.hwDecoder.firstOrNull(), targetSize) // Hw accel
        val frameGrabber = requireNotNull(state.frameGrabber) { "Frame grabber not initialized!" }
        var startTs = -1L
        var lastDisplayFrameCount = 0L
        var lastDecodedFrameCount = 0L
        var lastTs = -1L
        while (isActive) {
            withFrameMillis { currentTs ->
                if (startTs < 0) {
                    startTs = currentTs
                    lastTs = currentTs
                }
                if (state.seekPosition >= 0) {
                    val seekMillis: Long = (stream.durationMillis * state.seekPosition.toDouble()).toLong()
                    startTs = currentTs - seekMillis
                    state.seekPosition = -1f
                }
                val pos = currentTs - startTs
                frameGrabber.grabNextFrame(pos)
                videoImage.value = frameGrabber.composeImage
                state.time = pos
                state.progress = (pos.toDouble() / stream.durationMillis.toDouble()).toFloat()
                frame++
                if (frame % 60 == 0) {
                    val time = (currentTs - lastTs).toFloat() / 1000f
                    val newDisplayFrameCount = frameGrabber.bitmapFrameCounter
                    val displayFrameCount = newDisplayFrameCount - lastDisplayFrameCount
                    state.displayFPS = displayFrameCount.toFloat() / time
                    val newDecodedFrameCount = frameGrabber.decodedFrameCounter
                    val decodedFrameCount = newDecodedFrameCount - lastDecodedFrameCount
                    state.decodedFPS = decodedFrameCount.toFloat() / time
                    lastDisplayFrameCount = newDisplayFrameCount
                    lastDecodedFrameCount = newDecodedFrameCount
                    lastTs = currentTs
                }
            }
        }
        state.stop()
        state.close()
    }
    videoImage.value?.let { image ->
        Spacer(modifier.drawBehind {
            val scaleW = size.width / image.width.toFloat()
            val scaleH = size.height / image.height.toFloat()
            scale(
                scale = min(scaleH, scaleW),
                pivot = Offset.Zero
            ) {
                println("$image")
                drawImage(image)
            }
        })
    }
}
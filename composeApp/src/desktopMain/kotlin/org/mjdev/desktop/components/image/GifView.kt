package org.mjdev.desktop.components.image

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.helpers.gif.GifDecoder
import kotlinx.coroutines.delay
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream

@Suppress("FunctionName")
@Composable
fun GifView(
    modifier: Modifier = Modifier,
    src: String = "",
    onAnimationFinish: () -> Unit = {}
) = BoxWithConstraints {
    val file by remember(src) { mutableStateOf(File(src)) }
    val streamData by remember(file) { mutableStateOf(FileInputStream(file)) }
    val gifDecoder by remember(streamData) { mutableStateOf(GifDecoder().apply { read(streamData) }) }
    val framesCount by remember(gifDecoder) { mutableStateOf(gifDecoder.getFrameCount()) }
    var currentFrame by remember(gifDecoder) { mutableStateOf(0) }
    var decodedImage by remember { mutableStateOf<BufferedImage?>(null) }
    Canvas(
        modifier = modifier
    ) {
        decodedImage?.toComposeImageBitmap()?.let { img ->
            drawImage(
                image = img,
                srcSize = IntSize(img.width, img.height),
                dstSize = IntSize(constraints.maxWidth, constraints.maxHeight),
            )
        }
    }
    LaunchedEffect(currentFrame, decodedImage) {
        decodedImage = gifDecoder.getFrame(currentFrame)
        delay(gifDecoder.getDelay(currentFrame).toLong())
        var nextFrame = currentFrame + 1
        if (nextFrame > framesCount) {
            nextFrame = 0
            onAnimationFinish()
        }
        currentFrame = nextFrame
    }
}

// todo
@Suppress("unused")
@Preview
@Composable
fun PreviewGifView() = preview {
    GifView()
}

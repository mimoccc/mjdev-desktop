package org.mjdev.desktop.helpers.gif

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces

@Suppress("MemberVisibilityCanBePrivate")
class GifImage(
    val src: String,
) : ImageBitmap {
    private val decoder: GifDecoder = GifDecoder.fromPathOrUrl(src)

    var currentFrame: Int = 0
    val duration: Long get() = decoder.getDuration()

    override val width: Int = decoder.getWidth()
    override val height: Int = decoder.getHeight()
    override val hasAlpha: Boolean = true
    override val colorSpace: ColorSpace = ColorSpaces.Srgb
    override val config: ImageBitmapConfig = ImageBitmapConfig.Argb8888

    override fun prepareToDraw() {
        currentFrame = 0
    }

    override fun readPixels(
        buffer: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        bufferOffset: Int,
        stride: Int,
    ) {
        decoder.getFrame(currentFrame)?.readPixels(
            buffer,
            startX,
            startY,
            width,
            height,
            bufferOffset,
            stride,
        )
    }

    override fun toString(): String = "GifImage(src='$src')"
}

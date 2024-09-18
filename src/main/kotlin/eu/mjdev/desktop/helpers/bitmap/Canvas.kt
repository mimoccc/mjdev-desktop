package eu.mjdev.desktop.helpers.bitmap

import androidx.compose.ui.geometry.Rect
import java.awt.Graphics2D
import java.awt.image.BufferedImage

@Suppress("unused")
class Canvas (bitmap: Bitmap) {
    private val canvasImage: BufferedImage = bitmap.image
    private val canvas: Graphics2D = canvasImage.createGraphics()

    fun drawBitmap(sourceBitmap: Bitmap, src: Rect, dst: Rect) {
        val sourceImage = sourceBitmap.image
        val sourceImageCropped :BufferedImage = sourceImage.getSubimage(
            src.left.toInt(),
            src.top.toInt(),
            src.width.toInt(),
            src.height.toInt()
        )
        canvas.drawImage(sourceImageCropped, null , dst.left.toInt(), dst.top.toInt())
    }
}
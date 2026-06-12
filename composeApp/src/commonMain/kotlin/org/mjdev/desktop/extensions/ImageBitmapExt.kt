package org.mjdev.desktop.extensions

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import org.mjdev.desktop.context.IDesktopContext

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object ImageBitmapExt {
    suspend fun IDesktopContext.loadPicture(
        src: Any?
    ): ImageBitmap?
}

var ImageBitmap.pixels: IntArray
    get() = IntArray(width * height).also { pixels -> readPixels(pixels) }
    set(value) = writePixels(value, 0, 0, width, height)

fun ImageBitmap.fillRect(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    c: Color
) {
    Canvas(this).drawRect(
        x.toFloat(),
        y.toFloat(),
        width.toFloat(),
        height.toFloat(),
        Paint().apply { color = c }
    )
}

fun ImageBitmap.writePixels(
    data: IntArray,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) {
    val paint = Paint()
    val canvas = Canvas(this)
    var index = 0
    for (j in 0 until height) {
        for (i in 0 until width) {
            val colorInt = data[index++]
            paint.color = Color(colorInt)
            canvas.drawRect(
                (x + i).toFloat(),
                (y + j).toFloat(),
                (x + i + 1).toFloat(),
                (y + j + 1).toFloat(),
                paint
            )
        }
    }
}

fun ImageBitmap.cut(
    x: Int,
    y: Int,
    width: Int,
    height: Int
): ImageBitmap {
    val srcPixels = this.pixels
    val result = ImageBitmap(width, height)
    val subPixels = IntArray(width * height)
    for (j in 0 until height) {
        for (i in 0 until width) {
            val srcIndex = (y + j) * this.width + (x + i)
            val dstIndex = j * width + i
            subPixels[dstIndex] = srcPixels.getOrElse(srcIndex) { 0 }
        }
    }
    result.pixels = subPixels
    return result
}

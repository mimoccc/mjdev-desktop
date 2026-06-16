package org.mjdev.desktop.helpers.image

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import org.mjdev.desktop.extensions.Colors.SuperDarkGray

@Suppress("MemberVisibilityCanBePrivate")
object ImageUtils {
    val ImageBitmap.histogram: Map<Color, Int>
        get() =
            mutableMapOf<Color, Int>().apply {
                val pixmap = toPixelMap(0, 0, width, height)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        val color = pixmap[x, y]
                        val colorCount = this[color] ?: 0
                        this[color] = (colorCount + 1)
                    }
                }
            }

    val ImageBitmap.topMostColor: Color
        get() =
            histogram
                .filter { c -> c.key.alpha != 0F }
                .maxByOrNull { (_, occurrence) -> occurrence }
                ?.key ?: Color.SuperDarkGray

    /**
     * Most frequent non-transparent color in a rectangular region of a packed-ARGB pixel buffer.
     * Operates directly on an [IntArray] (one bulk `readPixels` for the whole image) with an
     * Int-keyed histogram — no intermediate [ImageBitmap] allocation and no `Color` boxing per
     * pixel. Equivalent result to [topMostColor] on a `cut()` sub-bitmap.
     */
    fun dominantArgbColor(
        pixels: IntArray,
        imageWidth: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): Color {
        val counts = mutableMapOf<Int, Int>()
        for (j in 0 until height) {
            val rowBase = (y + j) * imageWidth + x
            for (i in 0 until width) {
                val argb = pixels.getOrElse(rowBase + i) { 0 }
                if ((argb ushr 24) == 0) continue // fully transparent
                counts[argb] = (counts[argb] ?: 0) + 1
            }
        }
        val top = counts.maxByOrNull { (_, occurrence) -> occurrence }?.key ?: return Color.SuperDarkGray
        return Color(top)
    }
}

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
}

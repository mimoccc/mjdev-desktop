package org.psippr.desktop.managers.theme.linux.gtk

import androidx.compose.ui.graphics.Color
import org.psippr.desktop.extensions.StringExt.hexRgb
import java.util.Locale
import kotlin.math.roundToInt

/** Color to CSS text, understood by GTK2 rc, GTK3, GTK4 and GNOME Shell (St) parsers. */
object CssColor {
    /** Max value of one 8 bit channel. */
    private const val CHANNEL_MAX = 255

    /** Alpha format with enough precision for 8 bit alpha. */
    private const val ALPHA_FORMAT = "%.3f"

    /** `#rrggbb` for opaque colors, `rgba(r, g, b, a)` otherwise. */
    fun css(color: Color): String = if (color.alpha >= 1f) {
        color.hexRgb
    } else {
        val r = (color.red * CHANNEL_MAX).roundToInt()
        val g = (color.green * CHANNEL_MAX).roundToInt()
        val b = (color.blue * CHANNEL_MAX).roundToInt()
        val a = String.format(Locale.ROOT, ALPHA_FORMAT, color.alpha)
        "rgba($r, $g, $b, $a)"
    }

    /** [color] with the given [alpha], as CSS. */
    fun css(
        color: Color,
        alpha: Float,
    ): String = css(color.copy(alpha = alpha))
}

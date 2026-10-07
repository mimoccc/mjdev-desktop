package org.mjdev.desktop.managers.theme.linux.shell

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.min

/**
 * The fixed GNOME 47+ system accents (`org.gnome.desktop.interface accent-color`).
 *
 * Choosing the nearest one makes running libadwaita apps (also sandboxed flatpaks, via the settings
 * portal) and GNOME Shell follow the wallpaper live, without a restart.
 *
 * @property key gsettings value
 * @property color reference color of the accent (libadwaita values)
 */
enum class GnomeAccent(
    val key: String,
    val color: Color,
) {
    /** Blue. */
    BLUE("blue", Color(0xFF3584E4)),

    /** Teal. */
    TEAL("teal", Color(0xFF2190A4)),

    /** Green. */
    GREEN("green", Color(0xFF3A944A)),

    /** Yellow. */
    YELLOW("yellow", Color(0xFFC88800)),

    /** Orange. */
    ORANGE("orange", Color(0xFFED5B00)),

    /** Red. */
    RED("red", Color(0xFFE62D42)),

    /** Pink. */
    PINK("pink", Color(0xFFD56199)),

    /** Purple. */
    PURPLE("purple", Color(0xFF9141AC)),

    /** Slate - the grey accent, used for colorless palettes. */
    SLATE("slate", Color(0xFF6F8396)),
    ;

    companion object {
        /** Below this HSB saturation a color counts as grey and maps to [SLATE]. */
        const val GREY_SATURATION = 0.2f

        /** Full hue circle in HSB units. */
        private const val HUE_CIRCLE = 1f

        /** Max value of one 8 bit channel. */
        private const val CHANNEL_MAX = 255

        /** Index of hue in the HSB array. */
        private const val HUE = 0

        /** Index of saturation in the HSB array. */
        private const val SATURATION = 1

        /** Accent closest in hue to [color]; [SLATE] for greys. */
        fun nearest(color: Color): GnomeAccent {
            val hsb = hsb(color)
            if (hsb[SATURATION] < GREY_SATURATION) return SLATE
            return entries
                .filter { accent -> accent != SLATE }
                .minBy { accent -> hueDistance(hsb[HUE], hsb(accent.color)[HUE]) }
        }

        private fun hueDistance(
            a: Float,
            b: Float,
        ): Float = abs(a - b).let { d -> min(d, HUE_CIRCLE - d) }

        private fun hsb(color: Color): FloatArray = java.awt.Color.RGBtoHSB(
            (color.red * CHANNEL_MAX).toInt(),
            (color.green * CHANNEL_MAX).toInt(),
            (color.blue * CHANNEL_MAX).toInt(),
            null,
        )
    }
}

package org.psippr.desktop.managers.palette

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Picks the accent color (icons, widget rings, selection) from the most colorful part of the
 * wallpaper instead of only lightening the dark background, which gave a near-white grey for
 * every dark wallpaper. The lightness is pushed away from the background so it stays readable.
 */
object PaletteAccent {
    /** Below this HSL saturation a sample counts as grey (no usable accent). */
    const val MIN_SATURATION = 0.2f

    /** Minimum lightness of an accent on a dark background. */
    const val MIN_LIGHTNESS_ON_DARK = 0.62f

    /** Maximum lightness of an accent on a dark background, so pale colors keep their tint. */
    const val MAX_LIGHTNESS_ON_DARK = 0.75f

    /** Maximum lightness of an accent on a light background. */
    const val MAX_LIGHTNESS_ON_LIGHT = 0.35f

    /** Upper limit of the accent saturation, keeps it from looking neon. */
    const val MAX_SATURATION = 0.85f

    /** Number of hue sectors of the HSL color wheel. */
    private const val HUE_SECTORS = 6f

    /** Degrees of one hue sector. */
    private const val SECTOR_DEGREES = 60f

    /** Hue sector offset when green is the strongest channel. */
    private const val GREEN_SECTOR_OFFSET = 2f

    /** Hue sector offset when blue is the strongest channel. */
    private const val BLUE_SECTOR_OFFSET = 4f

    /** Degrees of the full color wheel. */
    private const val FULL_CIRCLE = 360f

    /**
     * Readable accent from the most saturated of [samples] for a [background] that is light when
     * [lightBackground], or null when all samples are grey (the caller then keeps the old tint).
     */
    fun pick(
        samples: List<Color>,
        lightBackground: Boolean,
    ): Color? {
        val best = samples.maxByOrNull { color -> color.toHsl().saturation } ?: return null
        val hsl = best.toHsl()
        if (hsl.saturation < MIN_SATURATION) return null
        val lightness =
            if (lightBackground) {
                min(hsl.lightness, MAX_LIGHTNESS_ON_LIGHT)
            } else {
                hsl.lightness.coerceIn(MIN_LIGHTNESS_ON_DARK, MAX_LIGHTNESS_ON_DARK)
            }
        return Color.hsl(hsl.hue, min(hsl.saturation, MAX_SATURATION), lightness)
    }

    /** HSL form of a color: hue in degrees (0..360), saturation and lightness in 0..1. */
    data class Hsl(
        val hue: Float,
        val saturation: Float,
        val lightness: Float,
    )

    /** Converts this color to [Hsl]. */
    fun Color.toHsl(): Hsl {
        val maxC = max(red, max(green, blue))
        val minC = min(red, min(green, blue))
        val delta = maxC - minC
        val lightness = (maxC + minC) / 2f
        if (delta == 0f) return Hsl(0f, 0f, lightness)
        val saturation = delta / (1f - abs(2f * lightness - 1f))
        val sector =
            when (maxC) {
                red -> ((green - blue) / delta).mod(HUE_SECTORS)
                green -> (blue - red) / delta + GREEN_SECTOR_OFFSET
                else -> (red - green) / delta + BLUE_SECTOR_OFFSET
            }
        return Hsl((sector * SECTOR_DEGREES).mod(FULL_CIRCLE), saturation.coerceIn(0f, 1f), lightness)
    }
}

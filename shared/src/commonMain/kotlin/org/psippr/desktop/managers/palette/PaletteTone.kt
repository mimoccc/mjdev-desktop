package org.psippr.desktop.managers.palette

import androidx.compose.ui.graphics.Color
import org.psippr.desktop.managers.palette.PaletteAccent.toHsl
import kotlin.math.max
import kotlin.math.min

/**
 * Applies the user's [ThemeMode] and [ThemeAccent] to the colors taken from the wallpaper. The
 * hue of the wallpaper stays; a forced mode only moves the background's lightness (and calms its
 * saturation, so the result is surely light or dark) and a fixed accent replaces the wallpaper's.
 */
object PaletteTone {
    /** Highest lightness of a background in [ThemeMode.DARK]. */
    const val DARK_MAX_LIGHTNESS = 0.16f

    /** Highest saturation of a background in [ThemeMode.DARK]. */
    const val DARK_MAX_SATURATION = 0.45f

    /** Lowest lightness of a background in [ThemeMode.LIGHT]. */
    const val LIGHT_MIN_LIGHTNESS = 0.9f

    /** Highest saturation of a background in [ThemeMode.LIGHT]. */
    const val LIGHT_MAX_SATURATION = 0.3f

    /** The wallpaper's [background] in [mode]. */
    fun background(
        background: Color,
        mode: ThemeMode,
    ): Color {
        val hsl = background.toHsl()
        return when (mode) {
            ThemeMode.WALLPAPER -> background
            ThemeMode.DARK ->
                Color.hsl(
                    hsl.hue,
                    min(hsl.saturation, DARK_MAX_SATURATION),
                    min(hsl.lightness, DARK_MAX_LIGHTNESS),
                )
            ThemeMode.LIGHT ->
                Color.hsl(
                    hsl.hue,
                    min(hsl.saturation, LIGHT_MAX_SATURATION),
                    max(hsl.lightness, LIGHT_MIN_LIGHTNESS),
                )
        }
    }

    /**
     * The fixed color of [accent] made readable on a background that is light when
     * [lightBackground]; null for [ThemeAccent.WALLPAPER] (the wallpaper's accent is used then).
     */
    fun accent(
        accent: ThemeAccent,
        lightBackground: Boolean,
    ): Color? = accent.color?.let { color -> PaletteAccent.pick(listOf(color), lightBackground) }
}

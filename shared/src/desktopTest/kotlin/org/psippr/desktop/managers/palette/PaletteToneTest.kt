package org.psippr.desktop.managers.palette

import androidx.compose.ui.graphics.Color
import org.psippr.desktop.extensions.Colors.isLightColor
import org.psippr.desktop.managers.palette.PaletteAccent.toHsl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PaletteToneTest {
    /** Wallpaper corners across the color wheel, light and dark, saturated and grey. */
    private val wallpapers =
        listOf(
            Color(0xFF0000FF),
            Color(0xFFFFFF00),
            Color(0xFF00FF00),
            Color(0xFFFF0000),
            Color(0xFF202020),
            Color(0xFFF0F0F0),
            Color(0xFF3355AA),
            Color(0xFFEECCAA),
        )

    /** Regression: the theme tab's mode must make any wallpaper surely dark or surely light. */
    @Test
    fun forcedModeIsDarkOrLightForEveryWallpaper() {
        wallpapers.forEach { wallpaper ->
            assertFalse(PaletteTone.background(wallpaper, ThemeMode.DARK).isLightColor, "dark from $wallpaper")
            assertTrue(PaletteTone.background(wallpaper, ThemeMode.LIGHT).isLightColor, "light from $wallpaper")
        }
    }

    @Test
    fun forcedModeKeepsTheWallpaperHue() {
        val blue = Color(0xFF3355AA)
        val hue = blue.toHsl().hue
        assertEquals(hue, PaletteTone.background(blue, ThemeMode.DARK).toHsl().hue, HUE_TOLERANCE)
        assertEquals(hue, PaletteTone.background(blue, ThemeMode.LIGHT).toHsl().hue, HUE_TOLERANCE)
    }

    @Test
    fun wallpaperModeLeavesTheBackgroundAlone() {
        wallpapers.forEach { wallpaper ->
            assertEquals(wallpaper, PaletteTone.background(wallpaper, ThemeMode.WALLPAPER))
        }
    }

    @Test
    fun fixedAccentIsReadableOnBothBackgrounds() {
        ThemeAccent.entries.filter { it.color != null }.forEach { accent ->
            val onDark = assertNotNull(PaletteTone.accent(accent, lightBackground = false)).toHsl().lightness
            val onLight = assertNotNull(PaletteTone.accent(accent, lightBackground = true)).toHsl().lightness
            assertTrue(onDark >= PaletteAccent.MIN_LIGHTNESS_ON_DARK - LIGHTNESS_TOLERANCE, "$accent on dark")
            assertTrue(onLight <= PaletteAccent.MAX_LIGHTNESS_ON_LIGHT + LIGHTNESS_TOLERANCE, "$accent on light")
        }
    }

    @Test
    fun wallpaperAccentLeavesThePickToTheWallpaper() {
        assertNull(PaletteTone.accent(ThemeAccent.WALLPAPER, lightBackground = false))
    }

    @Test
    fun unknownStoredNamesFallBackToTheWallpaper() {
        assertEquals(ThemeMode.WALLPAPER, ThemeMode.of(null))
        assertEquals(ThemeMode.WALLPAPER, ThemeMode.of("SEPIA"))
        assertEquals(ThemeAccent.WALLPAPER, ThemeAccent.of("GOLD"))
        assertEquals(ThemeAccent.TEAL, ThemeAccent.of("TEAL"))
    }

    private companion object {
        /** Hue rounding difference after the HSL round trip, in degrees. */
        const val HUE_TOLERANCE = 1.5f

        /** Lightness rounding difference after the HSL round trip. */
        const val LIGHTNESS_TOLERANCE = 0.01f
    }
}

package org.mjdev.desktop.managers.palette

import androidx.compose.ui.graphics.Color
import org.mjdev.desktop.managers.palette.PaletteAccent.toHsl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Regression tests: the accent must follow the wallpaper's colors, not stay a light grey. */
class PaletteAccentTest {
    @Test
    fun picksTheMostColorfulSampleAndKeepsItsHue() {
        val accent = assertNotNull(PaletteAccent.pick(listOf(DARK_GREY, TEAL, MUTED_BLUE), lightBackground = false))
        assertEquals(TEAL.toHsl().hue, accent.toHsl().hue, HUE_TOLERANCE)
    }

    @Test
    fun accentOnDarkBackgroundIsLightEnoughToRead() {
        val accent = assertNotNull(PaletteAccent.pick(listOf(DEEP_RED), lightBackground = false))
        assertTrue(accent.toHsl().lightness >= PaletteAccent.MIN_LIGHTNESS_ON_DARK - LIGHTNESS_TOLERANCE)
    }

    @Test
    fun paleColorOnDarkBackgroundKeepsItsTint() {
        val accent = assertNotNull(PaletteAccent.pick(listOf(PALE_YELLOW), lightBackground = false))
        assertTrue(accent.toHsl().lightness <= PaletteAccent.MAX_LIGHTNESS_ON_DARK + LIGHTNESS_TOLERANCE)
    }

    @Test
    fun accentOnLightBackgroundIsDarkEnoughToRead() {
        val accent = assertNotNull(PaletteAccent.pick(listOf(PALE_YELLOW), lightBackground = true))
        assertTrue(accent.toHsl().lightness <= PaletteAccent.MAX_LIGHTNESS_ON_LIGHT + LIGHTNESS_TOLERANCE)
    }

    @Test
    fun greyWallpaperGivesNoAccent() {
        assertNull(PaletteAccent.pick(listOf(DARK_GREY, Color.Gray, Color.White), lightBackground = false))
        assertNull(PaletteAccent.pick(emptyList(), lightBackground = false))
    }

    @Test
    fun hslOfPrimaryColors() {
        assertEquals(0f, Color.Red.toHsl().hue, HUE_TOLERANCE)
        assertEquals(GREEN_HUE, Color.Green.toHsl().hue, HUE_TOLERANCE)
        assertEquals(BLUE_HUE, Color.Blue.toHsl().hue, HUE_TOLERANCE)
    }

    private companion object {
        /** Sample wallpaper colors. */
        val DARK_GREY = Color(0xFF202020)
        val TEAL = Color(0xFF035D5E)
        val MUTED_BLUE = Color(0xFF3A4A5A)
        val DEEP_RED = Color(0xFF5A0A0A)
        val PALE_YELLOW = Color(0xFFF5EBA0)

        /** Expected hues in degrees. */
        const val GREEN_HUE = 120f
        const val BLUE_HUE = 240f

        /** Allowed rounding differences. */
        const val HUE_TOLERANCE = 1f
        const val LIGHTNESS_TOLERANCE = 0.01f
    }
}

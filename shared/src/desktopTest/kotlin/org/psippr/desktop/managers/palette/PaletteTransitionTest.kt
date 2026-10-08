package org.psippr.desktop.managers.palette

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** Regression tests: palette colors must move with the wallpaper fade instead of jumping. */
class PaletteTransitionTest {
    @Test
    fun startsAtCurrentAndEndsExactlyOnTarget() {
        assertEquals(START, PaletteTransition.colorAt(START, TARGET, 0f))
        assertEquals(TARGET, PaletteTransition.colorAt(START, TARGET, 1f))
    }

    @Test
    fun passesThroughInBetweenColors() {
        val middle = PaletteTransition.colorAt(START, TARGET, HALF)
        assertNotEquals(START, middle)
        assertNotEquals(TARGET, middle)
        assertTrue(middle.red > START.red && middle.red < TARGET.red)
    }

    @Test
    fun stepsCoverTheWholeFadeAndNeverZero() {
        assertEquals(1, PaletteTransition.steps(0L))
        assertEquals((FADE_MS / PaletteTransition.FRAME_MS).toInt(), PaletteTransition.steps(FADE_MS))
    }

    private companion object {
        /** Sample colors of two wallpapers. */
        val START = Color.Black
        val TARGET = Color.White

        /** Middle of the transition. */
        const val HALF = 0.5f

        /** Sample fade duration, in milliseconds. */
        const val FADE_MS = 7_500L
    }
}

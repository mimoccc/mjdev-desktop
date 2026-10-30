/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.psippr.compositor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Regression tests: a new app window opens centered at 90 % of the output. */
class WindowPlacementTest {
    @Test
    fun fullHdOutputGetsCenteredNinetyPercent() {
        assertEquals(
            WindowRect(x = 96, y = 54, width = 1728, height = 972),
            WindowPlacement.initial(FULL_HD_WIDTH, FULL_HD_HEIGHT),
        )
    }

    @Test
    fun unknownOutputSizeLeavesTheWindowAlone() {
        assertNull(WindowPlacement.initial(0, 0))
    }

    /** Regression: gnome-calculator remembered at 841,267 360x616 opened below a 1280x720 output. */
    @Test
    fun rememberedWindowBelowTheOutputMovesUp() {
        assertEquals(
            WindowRect(x = 841, y = 104, width = 360, height = 616),
            WindowPlacement.fit(WindowRect(841, 267, 360, 616), NESTED_WIDTH, NESTED_HEIGHT),
        )
    }

    @Test
    fun rememberedWindowLargerThanTheOutputShrinksToIt() {
        assertEquals(
            WindowRect(x = 0, y = 0, width = NESTED_WIDTH, height = NESTED_HEIGHT),
            WindowPlacement.fit(WindowRect(133, 45, 1603, 1043), NESTED_WIDTH, NESTED_HEIGHT),
        )
    }

    @Test
    fun rememberedWindowLeftOfTheOutputMovesRight() {
        assertEquals(
            WindowRect(x = 0, y = 186, width = 1158, height = 434),
            WindowPlacement.fit(WindowRect(-108, 186, 1158, 434), NESTED_WIDTH, NESTED_HEIGHT),
        )
    }

    @Test
    fun rememberedWindowOnTheOutputStays() {
        val rect = WindowRect(64, 36, 1152, 648)
        assertEquals(rect, WindowPlacement.fit(rect, NESTED_WIDTH, NESTED_HEIGHT))
    }

    @Test
    fun unknownOutputSizeCannotFit() {
        assertNull(WindowPlacement.fit(WindowRect(0, 0, 1, 1), 0, 0))
    }

    private companion object {
        /** Width of the 1280x720 nested test output. */
        const val NESTED_WIDTH = 1280

        /** Height of the 1280x720 nested test output. */
        const val NESTED_HEIGHT = 720

        /** Width of a 1920x1080 output. */
        const val FULL_HD_WIDTH = 1920

        /** Height of a 1920x1080 output. */
        const val FULL_HD_HEIGHT = 1080
    }
}

package org.psippr.desktop.state

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BarOverlapTest {
    private val strip = DesktopState.ScreenRect(0.0, STRIP_TOP, SCREEN_WIDTH, SCREEN_HEIGHT)

    /** Regression: a 90 % window reaching into the dock strip must hide the dock. */
    @Test
    fun windowReachingIntoTheDockOverlaps() {
        val window = DesktopState.ScreenRect(64.0, 36.0, 1216.0, 684.0)
        assertTrue(BarOverlap.overlaps(listOf(window), strip))
    }

    @Test
    fun windowEndingWhereTheDockStartsLeavesItFree() {
        val window = DesktopState.ScreenRect(64.0, 0.0, 1216.0, STRIP_TOP)
        assertFalse(BarOverlap.overlaps(listOf(window), strip))
    }

    @Test
    fun noWindowsLeaveTheDockFree() {
        assertFalse(BarOverlap.overlaps(emptyList(), strip))
    }

    @Test
    fun windowBesideTheDockLeavesItFree() {
        val window = DesktopState.ScreenRect(SCREEN_WIDTH, STRIP_TOP, SCREEN_WIDTH + 100.0, SCREEN_HEIGHT)
        assertFalse(BarOverlap.overlaps(listOf(window), strip))
    }

    private companion object {
        /** Width of the 1280x720 nested test screen. */
        const val SCREEN_WIDTH = 1280.0

        /** Height of the 1280x720 nested test screen. */
        const val SCREEN_HEIGHT = 720.0

        /** Top of the 80 high shown dock on that screen. */
        const val STRIP_TOP = 640.0
    }
}

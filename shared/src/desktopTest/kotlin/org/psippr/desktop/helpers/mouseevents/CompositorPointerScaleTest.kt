package org.psippr.desktop.helpers.mouseevents

import java.awt.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompositorPointerScaleTest {
    /**
     * Regression: on a 1280 px output with a 1.12 AWT scale the screen is 1143 wide for the
     * shell, so its 12 wide control center strip starts at 1131. The compositor pointer at 1131 px
     * (a title bar button of a 90 % app window) must not land in that strip.
     */
    @Test
    fun appWindowButtonIsOutsideTheControlCenterStrip() {
        val screenWidth = (OUTPUT_WIDTH / UI_SCALE).toInt()
        val stripStart = screenWidth - STRIP_WIDTH
        val button = CompositorPointerScale.toAwt(BUTTON_X, 0, UI_SCALE, UI_SCALE)
        val edge = CompositorPointerScale.toAwt(OUTPUT_WIDTH - 1, 0, UI_SCALE, UI_SCALE)
        assertTrue(button.x < stripStart, "button ${button.x} strip $stripStart")
        assertTrue(edge.x >= stripStart, "edge ${edge.x} strip $stripStart")
    }

    @Test
    fun withoutScaleThePixelsStay() {
        assertEquals(Point(BUTTON_X, BUTTON_Y), CompositorPointerScale.toAwt(BUTTON_X, BUTTON_Y, 1.0, 1.0))
    }

    @Test
    fun invalidScaleKeepsThePixels() {
        assertEquals(Point(BUTTON_X, BUTTON_Y), CompositorPointerScale.toAwt(BUTTON_X, BUTTON_Y, 0.0, Double.NaN))
    }

    @Test
    fun doubleScaleHalvesThePosition() {
        assertEquals(Point(BUTTON_X / 2, BUTTON_Y / 2), CompositorPointerScale.toAwt(BUTTON_X, BUTTON_Y, 2.0, 2.0))
    }

    private companion object {
        /** Width of the nested test output in pixels. */
        const val OUTPUT_WIDTH = 1280

        /** AWT UI scale measured in the nested run (1280 px shown as 1143). */
        const val UI_SCALE = 1.12

        /** Default control center divider width, the width of its reveal strip. */
        const val STRIP_WIDTH = 12

        /** Maximize button of a 90 % text editor window in the nested run. */
        const val BUTTON_X = 1131

        /** Height of that button. */
        const val BUTTON_Y = 34
    }
}

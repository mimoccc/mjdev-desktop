package org.psippr.desktop.components.tooltip

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Regression tests: the tooltip window never lands under the pointer (it caught the tab clicks). */
class TooltipPlacementTest {
    private val offset = DpOffset(16.dp, 24.dp)
    private val size = DpSize(360.dp, 96.dp)
    private val screen = DpSize(1280.dp, 720.dp)

    @Test
    fun pointerAtTheRightEdgeKeepsTheTooltipLeftOfIt() {
        val pointerX = 1256.dp
        val position = TooltipPlacement.position(pointerX, 176.dp, offset, size, screen)
        assertTrue(position.x + size.width <= pointerX)
    }

    @Test
    fun pointerAtTheBottomEdgeKeepsTheTooltipAboveIt() {
        val pointerY = 700.dp
        val position = TooltipPlacement.position(100.dp, pointerY, offset, size, screen)
        assertTrue(position.y + size.height <= pointerY)
    }

    /**
     * Regression: flipped left of a control center tab icon, the bubble must sit at the window's
     * right side (next to the icon), not at its left edge 360 dp away.
     */
    @Test
    fun flippedTooltipDrawsTheBubbleAtThePointerSide() {
        val spot = TooltipPlacement.spot(1256.dp, 176.dp, offset, size, screen)
        assertEquals(Alignment.TopEnd, spot.alignment)
        assertEquals(TooltipPlacement.position(1256.dp, 176.dp, offset, size, screen), spot.position)
    }

    @Test
    fun cornerTooltipHugsTheBottomEnd() {
        assertEquals(Alignment.BottomEnd, TooltipPlacement.spot(1256.dp, 700.dp, offset, size, screen).alignment)
    }

    @Test
    fun roomyTooltipKeepsTheBubbleTopStart() {
        assertEquals(Alignment.TopStart, TooltipPlacement.spot(100.dp, 100.dp, offset, size, screen).alignment)
    }

    @Test
    fun roomyPointerGetsTheTooltipBelowRight() {
        assertEquals(DpOffset(116.dp, 124.dp), TooltipPlacement.position(100.dp, 100.dp, offset, size, screen))
    }
}

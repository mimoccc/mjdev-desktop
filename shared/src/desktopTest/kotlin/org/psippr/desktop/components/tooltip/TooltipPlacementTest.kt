package org.psippr.desktop.components.tooltip

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

    @Test
    fun roomyPointerGetsTheTooltipBelowRight() {
        assertEquals(DpOffset(116.dp, 124.dp), TooltipPlacement.position(100.dp, 100.dp, offset, size, screen))
    }
}

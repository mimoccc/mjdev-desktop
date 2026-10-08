package org.psippr.desktop.components.draggable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression tests: a restored widget position must stay on screen, e.g. after a smaller screen. */
class DragBoundsTest {
    @Test
    fun positionInsideParentIsKept() {
        assertEquals(INSIDE, DragBounds.clamp(INSIDE, ORIGIN, SIZE, PARENT))
    }

    @Test
    fun positionOutsideParentIsPulledBack() {
        // origin 500 + size 100 in a 1000 wide parent: offset may go from -500 to +400
        assertEquals(Offset(400f, -300f), DragBounds.clamp(Offset(900f, -900f), ORIGIN, SIZE, PARENT))
    }

    @Test
    fun viewLargerThanParentSticksToStart() {
        assertEquals(Offset(-500f, -300f), DragBounds.clamp(INSIDE, ORIGIN, IntSize(2000, 2000), PARENT))
    }

    private companion object {
        /** Sample layout: a 100x100 view at (500, 300) in a 1000x800 parent. */
        val ORIGIN = Offset(500f, 300f)
        val SIZE = IntSize(100, 100)
        val PARENT = IntSize(1000, 800)

        /** An offset that keeps the view inside the parent. */
        val INSIDE = Offset(-100f, 50f)
    }
}

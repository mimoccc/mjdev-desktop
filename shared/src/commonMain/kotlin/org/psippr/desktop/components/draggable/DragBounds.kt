package org.psippr.desktop.components.draggable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

/** Keeps a dragged view inside its parent, e.g. a saved position after the screen got smaller. */
object DragBounds {
    /**
     * [offset] limited so a view of [size], laid out at [origin] in a parent of [parentSize],
     * stays fully inside the parent. A view larger than the parent sticks to the parent start.
     */
    fun clamp(
        offset: Offset,
        origin: Offset,
        size: IntSize,
        parentSize: IntSize,
    ): Offset = Offset(
        x = clampAxis(offset.x, origin.x, size.width, parentSize.width),
        y = clampAxis(offset.y, origin.y, size.height, parentSize.height),
    )

    private fun clampAxis(
        value: Float,
        origin: Float,
        size: Int,
        parentSize: Int,
    ): Float {
        val min = -origin
        val max = (parentSize - size) - origin
        return if (max < min) min else value.coerceIn(min, max)
    }
}

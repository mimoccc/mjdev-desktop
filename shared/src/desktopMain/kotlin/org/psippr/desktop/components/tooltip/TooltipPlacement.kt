package org.psippr.desktop.components.tooltip

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Places the tooltip window next to the pointer without ever covering it: where the window would
 * leave the screen it flips to the other side of the pointer instead of being pushed under it
 * (a window pushed under the pointer caught the clicks meant for the control center tabs).
 */
object TooltipPlacement {
    /** Window position for a pointer at [pointerX], [pointerY], kept on the screen. */
    fun position(
        pointerX: Dp,
        pointerY: Dp,
        cursorOffset: DpOffset,
        size: DpSize,
        containerSize: DpSize,
    ): DpOffset = DpOffset(
        x = axis(pointerX, cursorOffset.x, size.width, containerSize.width),
        y = axis(pointerY, cursorOffset.y, size.height, containerSize.height),
    )

    // after the pointer when it fits, else before it, clamped to the screen as the last resort
    private fun axis(
        pointer: Dp,
        offset: Dp,
        length: Dp,
        screen: Dp,
    ): Dp {
        val after = pointer + offset
        val start = if (after + length <= screen) after else pointer - offset - length
        return start.coerceIn(0.dp, (screen - length).coerceAtLeast(0.dp))
    }
}

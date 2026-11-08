package org.psippr.desktop.components.draggable

import androidx.compose.ui.geometry.Offset

/**
 * Tells a double click from the clicks' own event times, so a busy UI thread that handles the
 * events late cannot break it (a timer based detector then times out before the second click).
 * A press that moved further than [slop] before its release is a drag, not a click.
 *
 * @property timeoutMillis longest time between the two clicks
 * @property slop longest move, in pixels, of one click and between the two clicks
 */
class DoubleClickDetector(
    private val timeoutMillis: Long,
    private val slop: Float,
) {
    private var pressAt: Offset? = null
    private var lastClickTime: Long? = null
    private var lastClickAt: Offset = Offset.Zero

    /** A press at [position]. */
    fun onPress(position: Offset) {
        pressAt = position
    }

    /** A release at [position] at event time [timeMillis]; true when it ends a double click. */
    fun onRelease(
        timeMillis: Long,
        position: Offset,
    ): Boolean {
        val press = pressAt ?: return false
        pressAt = null
        if ((position - press).getDistance() > slop) {
            lastClickTime = null
            return false
        }
        val previous = lastClickTime
        val isDouble =
            previous != null &&
                timeMillis - previous <= timeoutMillis &&
                (position - lastClickAt).getDistance() <= slop
        // a double click starts over, so a third click is a single one again
        lastClickTime = if (isDouble) null else timeMillis
        lastClickAt = position
        return isDouble
    }
}

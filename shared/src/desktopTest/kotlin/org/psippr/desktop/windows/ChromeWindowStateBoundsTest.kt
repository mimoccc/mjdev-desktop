package org.psippr.desktop.windows

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression: the control center opened about 300 dp left of the screen edge (x = 652 instead
 * of 960) because its size was applied as a move relative to a stale position. Bounds are now
 * applied absolutely, so expanding and collapsing always land exactly where asked.
 */
class ChromeWindowStateBoundsTest {
    private val collapsedPosition = DpOffset(1268.dp, 0.dp)
    private val collapsedSize = DpSize(12.dp, 720.dp)
    private val expandedPosition = DpOffset(960.dp, 0.dp)
    private val expandedSize = DpSize(320.dp, 720.dp)

    @Test
    fun expandAndCollapseLandExactlyWhereAsked() = runBlocking {
        val state = ChromeWindowState(collapsedPosition, collapsedSize, WindowCloseAction.CLOSE)
        repeat(REPEATS) {
            state.applyBounds(expandedPosition, expandedSize)
            assertEquals(expandedPosition, state.position)
            assertEquals(expandedSize, state.size)
            state.applyBounds(collapsedPosition, collapsedSize)
            assertEquals(collapsedPosition, state.position)
            assertEquals(collapsedSize, state.size)
        }
    }

    private companion object {
        /** Open / close cycles: a relative move drifted further with every cycle. */
        const val REPEATS = 3
    }
}

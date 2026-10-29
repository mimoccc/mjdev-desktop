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

    private companion object {
        /** Width of a 1920x1080 output. */
        const val FULL_HD_WIDTH = 1920

        /** Height of a 1920x1080 output. */
        const val FULL_HD_HEIGHT = 1080
    }
}

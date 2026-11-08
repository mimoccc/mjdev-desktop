package org.psippr.desktop.state

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Regression: a button at the bottom of the control center (Ethernet Apply) closed the panel,
 * because that spot is also in the bar zone; only moving down outside the panel closes it.
 */
class BarZoneEntryTest {
    @Test
    fun pointerAtTheBottomOfTheOpenPanelKeepsItOpen() {
        assertFalse(BarZoneEntry.closesControlCenter(entered = true, open = true, overControlCenter = true))
    }

    @Test
    fun movingDownToTheBarBesideThePanelClosesIt() {
        assertTrue(BarZoneEntry.closesControlCenter(entered = true, open = true, overControlCenter = false))
    }

    @Test
    fun closedPanelIsLeftAlone() {
        assertFalse(BarZoneEntry.closesControlCenter(entered = true, open = false, overControlCenter = false))
    }
}

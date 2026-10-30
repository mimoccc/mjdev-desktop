package org.psippr.desktop.components.desktop

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The right-click menu maps its labels back to the items it was built from. */
class DesktopMenuItemTest {
    @Test
    fun labelGivesItsItem() {
        assertEquals(DesktopMenuItem.SORT_ICONS, DesktopMenuItem.of(DesktopMenuItem.SORT_ICONS.label))
    }

    @Test
    fun unknownLabelGivesNothing() {
        assertNull(DesktopMenuItem.of("item1"))
    }
}

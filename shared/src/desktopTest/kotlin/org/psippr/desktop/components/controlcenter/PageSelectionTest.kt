package org.psippr.desktop.components.controlcenter

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression: tabs were filtered once, so an Ethernet adapter plugged in later got no tab. Tabs
 * are re-checked on every open now; the open tab must stay open when others appear or go.
 */
class PageSelectionTest {
    private val withoutEthernet = listOf("Home", "Wifi", "Sound")
    private val withEthernet = listOf("Home", "Wifi", "Ethernet", "Sound")

    @Test
    fun openTabStaysWhenATabAppearsBeforeIt() {
        assertEquals(3, PageSelection.indexAfterChange(withoutEthernet, withEthernet, selected = 2))
    }

    @Test
    fun openTabStaysWhenATabGoesBeforeIt() {
        assertEquals(2, PageSelection.indexAfterChange(withEthernet, withoutEthernet, selected = 3))
    }

    @Test
    fun removedOpenTabFallsBackToTheFirst() {
        assertEquals(0, PageSelection.indexAfterChange(withEthernet, withoutEthernet, selected = 2))
    }
}

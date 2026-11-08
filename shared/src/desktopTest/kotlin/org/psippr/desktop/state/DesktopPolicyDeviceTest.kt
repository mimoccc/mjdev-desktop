package org.psippr.desktop.state

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression: the bar covered a connected device's screen; it steps aside until the pointer asks for it. */
class DesktopPolicyDeviceTest {
    @Test
    fun deviceScreenHidesTheBar() {
        assertFalse(DesktopPolicy.decide(DesktopInputs(deviceShown = true)).bar)
    }

    @Test
    fun hoverBringsTheBarBackOverADeviceScreen() {
        assertTrue(DesktopPolicy.decide(DesktopInputs(deviceShown = true, pointerInBarRevealZone = true)).bar)
    }

    @Test
    fun openMenuStillKeepsTheBarOverADeviceScreen() {
        assertTrue(DesktopPolicy.decide(DesktopInputs(deviceShown = true, menuOpen = true)).bar)
    }
}

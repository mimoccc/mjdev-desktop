package org.psippr.desktop.state

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression: while a notification popup is shown the bar steps aside until the pointer asks for it. */
class DesktopPolicyNotificationTest {
    @Test
    fun notificationHidesTheBar() {
        assertFalse(DesktopPolicy.decide(DesktopInputs(notificationShown = true)).bar)
    }

    @Test
    fun hoverBringsTheBarBackDuringANotification() {
        assertTrue(DesktopPolicy.decide(DesktopInputs(notificationShown = true, pointerInBarRevealZone = true)).bar)
    }

    @Test
    fun withoutNotificationTheBarStays() {
        assertTrue(DesktopPolicy.decide(DesktopInputs()).bar)
    }

    @Test
    fun openMenuStillKeepsTheBar() {
        assertTrue(DesktopPolicy.decide(DesktopInputs(notificationShown = true, menuOpen = true)).bar)
    }
}

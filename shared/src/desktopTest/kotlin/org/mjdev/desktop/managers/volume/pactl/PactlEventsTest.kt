package org.mjdev.desktop.managers.volume.pactl

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression tests: only device level pactl events may trigger a Sound page reload. */
class PactlEventsTest {
    @Test
    fun deviceEventsCount() {
        listOf(
            "Event 'change' on sink #58",
            "Event 'new' on source #63",
            "Event 'remove' on card #54",
            "Event 'change' on server #4294967295",
        ).forEach { line -> assertTrue(PactlEvents.isDeviceEvent(line), line) }
    }

    @Test
    fun streamEventsAreIgnored() {
        listOf(
            "Event 'change' on sink-input #120",
            "Event 'new' on source-output #121",
            "Event 'change' on client #99",
        ).forEach { line -> assertFalse(PactlEvents.isDeviceEvent(line), line) }
    }
}

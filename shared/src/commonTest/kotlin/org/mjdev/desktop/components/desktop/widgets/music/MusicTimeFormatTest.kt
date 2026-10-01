package org.mjdev.desktop.components.desktop.widgets.music

import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression tests of the `m:ss` label, including the unknown (zero / negative) duration. */
class MusicTimeFormatTest {
    @Test
    fun padsSecondsToTwoDigits() {
        assertEquals("3:05", formatMusicTime(185_000L))
    }

    @Test
    fun negativeCountsAsZero() {
        assertEquals("0:00", formatMusicTime(-1L))
    }
}

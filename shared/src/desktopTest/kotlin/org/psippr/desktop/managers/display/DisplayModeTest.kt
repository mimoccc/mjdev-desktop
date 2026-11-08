package org.psippr.desktop.managers.display

import kotlin.test.Test
import kotlin.test.assertEquals

class DisplayModeTest {
    @Test
    fun wholeRefreshHasNoDecimals() {
        assertEquals("1920 × 1080 @ 60 Hz", DisplayMode(1920, 1080, 60000).label)
    }

    @Test
    fun fractionalRefreshKeepsTwoDecimals() {
        assertEquals("1920 × 1080 @ 59.93 Hz", DisplayMode(1920, 1080, 59934).label)
        assertEquals("1600 × 900 @ 59.05 Hz", DisplayMode(1600, 900, 59050).label)
    }

    @Test
    fun unknownRefreshShowsTheSizeOnly() {
        assertEquals("1280 × 720", DisplayMode(1280, 720).label)
    }
}

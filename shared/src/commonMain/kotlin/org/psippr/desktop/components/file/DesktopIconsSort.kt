package org.psippr.desktop.components.file

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * "Sort icons" requests for the desktop folder view: every request puts the icons back into the
 * sorted grid (folders first, then by name), dropping where they were dragged to.
 */
object DesktopIconsSort {
    /** Count of requests so far; the folder view sorts again whenever it changes. */
    var requests by mutableIntStateOf(0)
        private set

    /** Asks the desktop folder view to sort its icons. */
    fun request() {
        requests++
    }
}

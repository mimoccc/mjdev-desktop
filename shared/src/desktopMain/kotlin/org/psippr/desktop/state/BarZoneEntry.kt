package org.psippr.desktop.state

/**
 * When moving the pointer down to the bar closes an open control center. The bar's zone is a
 * full-width strip at the bottom, so it also covers the bottom of the open control center; a
 * pointer there is using the control center (e.g. its Apply button), not going to the bar.
 */
object BarZoneEntry {
    /**
     * True when the pointer just [entered] the bar zone while the control center is [open] and
     * the pointer is not [overControlCenter].
     */
    fun closesControlCenter(
        entered: Boolean,
        open: Boolean,
        overControlCenter: Boolean,
    ): Boolean = entered && open && !overControlCenter
}

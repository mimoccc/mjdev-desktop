package org.psippr.desktop.managers.display.mutter

/**
 * A physical monitor of mutter's DisplayConfig.
 *
 * @property connector connector name (`eDP-1`, `HDMI-1`), its id when applying
 * @property modes modes it offers
 */
data class MutterMonitor(
    val connector: String,
    val modes: List<MutterMode>,
) {
    /** The mode it runs in now, null while it is off. */
    val currentMode: MutterMode?
        get() = modes.firstOrNull { mode -> mode.current }
}

package org.psippr.desktop.managers.display.mutter

/**
 * A logical monitor of mutter's DisplayConfig: a screen area shown by one or more monitors.
 *
 * @property x left edge in the layout
 * @property y top edge in the layout
 * @property scale UI scale
 * @property transform rotation / flip code
 * @property primary true for the main screen
 * @property connectors monitors showing this area
 */
data class MutterLogicalMonitor(
    val x: Int,
    val y: Int,
    val scale: Double,
    val transform: Int,
    val primary: Boolean,
    val connectors: List<String>,
)

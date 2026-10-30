package org.psippr.desktop.managers.display.mutter

/**
 * One mode of a monitor as mutter's DisplayConfig lists it.
 *
 * @property id mutter's mode id (`1920x1080@60.000`), passed back when applying
 * @property width horizontal pixels
 * @property height vertical pixels
 * @property refreshHz refresh rate in Hz
 * @property scales scales mutter allows with this mode
 * @property current true for the mode the monitor runs in now
 */
data class MutterMode(
    val id: String,
    val width: Int,
    val height: Int,
    val refreshHz: Double,
    val scales: List<Double>,
    val current: Boolean,
)

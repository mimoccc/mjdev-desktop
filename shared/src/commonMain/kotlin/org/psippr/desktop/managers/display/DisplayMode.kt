package org.psippr.desktop.managers.display

import kotlin.math.roundToInt

/**
 * One screen mode: resolution and refresh rate.
 *
 * @property width horizontal pixels
 * @property height vertical pixels
 * @property refreshMilliHz refresh rate in mHz (60000 = 60 Hz), 0 when unknown
 */
data class DisplayMode(
    val width: Int,
    val height: Int,
    val refreshMilliHz: Int = 0,
) {
    /** Human readable form, e.g. `1920 × 1080 @ 60 Hz` (refresh left out when unknown). */
    val label: String
        get() {
            val size = "$width$SIZE_SEPARATOR$height"
            return if (refreshMilliHz > 0) "$size$REFRESH_SEPARATOR${hertz()}$HERTZ" else size
        }

    // two decimals only where they tell modes apart (59.94 vs 60)
    private fun hertz(): String {
        val hundredths = (refreshMilliHz / MILLI_PER_HUNDREDTH.toDouble()).roundToInt()
        val whole = hundredths / HUNDREDTHS
        val rest = hundredths % HUNDREDTHS
        return if (rest == 0) "$whole" else "$whole.${rest.toString().padStart(2, '0')}"
    }

    private companion object {
        const val SIZE_SEPARATOR = " × "
        const val REFRESH_SEPARATOR = " @ "
        const val HERTZ = " Hz"

        /** mHz in a hundredth of a hertz. */
        const val MILLI_PER_HUNDREDTH = 10

        /** Hundredths in one hertz. */
        const val HUNDREDTHS = 100
    }
}

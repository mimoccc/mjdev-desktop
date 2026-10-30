package org.psippr.desktop.components.file

/** Size of the desktop icon grid. */
object IconGrid {
    /**
     * Columns needed for [count] icons in [rows] rows: rounded up, so every icon gets a cell of
     * its own (rounding down stacked the last icons onto each other).
     */
    fun columns(
        count: Int,
        rows: Int,
    ): Int {
        val safeRows = rows.coerceAtLeast(1)
        return ((count + safeRows - 1) / safeRows).coerceAtLeast(1)
    }
}

package org.psippr.desktop.components.controlcenter

/**
 * Keeps the open control center tab when the list of shown tabs changes (a tab appears because
 * its adapter or device was plugged in, or disappears because it was removed).
 */
object PageSelection {
    /**
     * Index in [new] of the page that was at [selected] in [old]; the first tab when that page is
     * no longer shown.
     */
    fun <T> indexAfterChange(
        old: List<T>,
        new: List<T>,
        selected: Int,
    ): Int = old.getOrNull(selected)?.let { page -> new.indexOf(page) }?.coerceAtLeast(0) ?: 0
}

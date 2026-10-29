package org.psippr.desktop.components.controlcenter

/** Which control center pages [NeighbourPages] keeps composed. */
object NeighbourPageIndices {
    /** Pages kept composed on each side of the shown one, so switching to a neighbour is instant. */
    private const val NEIGHBOUR_PAGES = 1

    /** Indices of the page at [shown] and its neighbours that exist among [count] pages, in order. */
    fun of(
        shown: Int,
        count: Int,
    ): List<Int> = ((shown - NEIGHBOUR_PAGES)..(shown + NEIGHBOUR_PAGES)).filter { index -> index in 0 until count }
}

package org.psippr.desktop.components.controlcenter

import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression tests: only the shown tab and its neighbours are composed, never all of them. */
class NeighbourPagesTest {
    @Test
    fun firstTabComposesItselfAndTheNext() {
        assertEquals(listOf(0, 1), NeighbourPageIndices.of(0, PAGE_COUNT))
    }

    @Test
    fun middleTabComposesPreviousAndNext() {
        assertEquals(listOf(3, 4, 5), NeighbourPageIndices.of(4, PAGE_COUNT))
    }

    @Test
    fun lastTabComposesThePreviousAndItself() {
        assertEquals(listOf(9, 10), NeighbourPageIndices.of(10, PAGE_COUNT))
    }

    private companion object {
        /** Number of control center tabs on a machine with wifi and bluetooth. */
        const val PAGE_COUNT = 11
    }
}

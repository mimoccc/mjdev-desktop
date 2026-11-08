package org.psippr.desktop.components.file

import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression: with 7 icons in 4 rows the grid had 1 column, so 3 icons were stacked on others. */
class IconGridTest {
    @Test
    fun everyIconGetsACell() {
        assertEquals(2, IconGrid.columns(count = 7, rows = 4))
    }

    @Test
    fun fullColumnsNeedNoExtraOne() {
        assertEquals(2, IconGrid.columns(count = 8, rows = 4))
    }

    @Test
    fun emptyDesktopStillHasAColumn() {
        assertEquals(1, IconGrid.columns(count = 0, rows = 4))
    }
}

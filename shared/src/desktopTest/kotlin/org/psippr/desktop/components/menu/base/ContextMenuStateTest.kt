package org.psippr.desktop.components.menu.base

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Regression: the desktop menu always opened at the top-left corner instead of at the pointer. */
class ContextMenuStateTest {
    @Test
    fun opensWhereItWasAsked() {
        val state = ContextMenuState(items = listOf("item"))
        val pointer = Offset(820f, 470f)
        state.show(pointer)
        assertTrue(state.menuExpanded.value)
        assertEquals(pointer, state.position.value)
    }
}

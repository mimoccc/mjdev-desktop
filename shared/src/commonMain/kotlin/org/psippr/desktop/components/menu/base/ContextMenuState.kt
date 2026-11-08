package org.psippr.desktop.components.menu.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset

class ContextMenuState(
    val menuExpanded: MutableState<Boolean> = mutableStateOf(false),
    val menuRenderCount: MutableState<Int> = mutableStateOf(0),
    val items: List<String> = listOf(),
    /** Where the menu opens, in pixels of the surface it is drawn on. */
    val position: MutableState<Offset> = mutableStateOf(Offset.Zero),
) {
    fun show() {
        menuExpanded.value = true
    }

    /** Opens the menu at [at], e.g. where the pointer was clicked. */
    fun show(at: Offset) {
        position.value = at
        show()
    }

    fun hide() {
        menuExpanded.value = false
    }

    companion object {
        @Composable
        fun rememberContextMenuState(vararg items: String) = remember {
            ContextMenuState(
                items = items.asList(),
            )
        }
    }
}

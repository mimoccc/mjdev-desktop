package eu.mjdev.desktop.components.menu.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class ContextMenuState(
    val menuExpanded: MutableState<Boolean> = mutableStateOf(false),
    val menuRenderCount: MutableState<Int> = mutableStateOf(0),
    val items: List<String> = listOf()
) {
    fun show() {
        menuExpanded.value = true
    }

    fun hide() {
        menuExpanded.value = false
    }

    companion object {
        @Composable
        fun rememberContextMenuState(vararg items: String) = remember {
            ContextMenuState(
                items = items.asList()
            )
        }
    }
}
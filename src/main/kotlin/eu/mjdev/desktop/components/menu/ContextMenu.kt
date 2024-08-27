package eu.mjdev.desktop.components.menu

import androidx.compose.foundation.background
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray

@Composable
fun ContextMenu(
    backgroundColor: Color = Color.SuperDarkGray,
    textColor: Color = Color.White,
    contextMenuState: ContextMenuState,
    onMenuItemClick: (item: String) -> Unit = {}
) = listOf(
    contextMenuState.menuRenderCount,
    contextMenuState.menuRenderCount.value - 1
).forEach { renderId ->
    val isActive = renderId == contextMenuState.menuRenderCount
    key(renderId) {
        CursorDropdownMenu(
            modifier = Modifier.background(backgroundColor),
            expanded = contextMenuState.menuExpanded.value && isActive,
            onDismissRequest = {
                if (isActive) {
                    contextMenuState.menuRenderCount.value += 1
                    contextMenuState.menuExpanded.value = false
                }
            },
        ) {
            contextMenuState.items.forEach { item ->
                DropdownMenuItem(
                    content = {
                        Text(
                            text = item,
                            color = textColor
                        )
                    },
                    onClick = {
                        onMenuItemClick(item)
                        contextMenuState.hide()
                    }
                )
            }
        }
    }
}

class ContextMenuState(
    val menuExpanded: MutableState<Boolean> = mutableStateOf(false),
    val menuRenderCount: MutableState<Int> = mutableStateOf(0),
    val items: List<String> = listOf<String>()
) {
    fun show() {
        menuExpanded.value = true
    }

    fun hide() {
        menuExpanded.value = false
    }

    companion object {
        @Composable
        fun rememberContextMenuState(vararg items: String) =
            ContextMenuState(
                items = items.asList()
            )
    }
}
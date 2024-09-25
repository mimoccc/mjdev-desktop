package eu.mjdev.desktop.components.menu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray

// todo
@Suppress("unused", "FunctionName")
@Preview
@Composable
fun ContextMenu(
    backgroundColor: Color = Color.SuperDarkGray,
    textColor: Color = Color.White,
    contextMenuState: ContextMenuState,
//    onShow: () -> Unit = {},
//    onHide: () -> Unit = {},
    onMenuItemClick: (item: String) -> Unit = {}
) = listOf(
    contextMenuState.menuRenderCount,
    contextMenuState.menuRenderCount.value - 1
).forEach { renderId ->
    val isActive = renderId == contextMenuState.menuRenderCount
    key(renderId) {
        CursorDropdownMenu(
            modifier = Modifier.background(color = backgroundColor, shape = RoundedCornerShape(16.dp)),
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
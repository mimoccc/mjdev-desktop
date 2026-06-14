package org.mjdev.desktop.components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.menu.base.ContextMenuState
import org.mjdev.desktop.components.menu.base.ContextMenuState.Companion.rememberContextMenuState
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.extensions.Compose.preview

// todo
@Suppress("unused", "FunctionName")
@Composable
fun ContextMenu(
    backgroundColor: Color = Color.SuperDarkGray,
    textColor: Color = Color.White,
    contextMenuState: ContextMenuState = rememberContextMenuState(),
//    onShow: () -> Unit = {},
//    onHide: () -> Unit = {},
    onMenuItemClick: (item: String) -> Unit = {},
) = listOf(
    contextMenuState.menuRenderCount,
    contextMenuState.menuRenderCount.value - 1,
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
                            color = textColor,
                        )
                    },
                    onClick = {
                        onMenuItemClick(item)
                        contextMenuState.hide()
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewContextMenu() =
    preview {
        ContextMenu()
    }

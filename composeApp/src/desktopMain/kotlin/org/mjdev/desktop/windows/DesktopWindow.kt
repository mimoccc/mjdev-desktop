package org.mjdev.desktop.windows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mjdev.desktop.components.menu.base.ContextMenuState
import org.mjdev.desktop.components.menu.base.ContextMenuState.Companion.rememberContextMenuState
import org.mjdev.desktop.components.sliding.base.VisibilityState
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext

@Suppress("FunctionName")
@Preview
@Composable
fun DesktopWindow(
    controlCenterState: VisibilityState = rememberVisibilityState(),
    panelState: VisibilityState = rememberVisibilityState(),
    menuState: VisibilityState = rememberVisibilityState(),
    onDesktopMenuShow: () -> Unit = {},
    contextMenuState: ContextMenuState = rememberContextMenuState("item1", "item2"),
    onTooltip: (item: Any?) -> Unit = {},
    content: @Composable BoxScope.() -> Unit = {},
) = withDesktopContext {
    FullScreenWindow(
        name = "DesktopWindow",
        alwaysOnBottom = true,
        onOpened = {
            window?.toBack()
            window?.isFocusable = false
        },
        onFocusChange = { focus ->
            if (focus) {
                window?.toBack()
                window?.isFocusable = false
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
//    ContextMenu(
//        contextMenuState = contextMenuState,
//        onMenuItemClick = { item -> log { "Context menu : $item" } },
//        onShow = { onDesktopMenuShow() },
//        onHide = {}
//    )
//    launchedEffect {
//        runCatching {
//            Notification("Hello!", "I just wanted to say hello :)").apply {
//                setUrgency(Notification.UrgencyCritical)
//            }.send()
//        }
//    }
    }
}

@Suppress("unused")
@Preview
@Composable
fun DesktopWindowPreview() = DesktopWindow()

package org.mjdev.desktop.windows

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.menu.base.ContextMenuState
import org.mjdev.desktop.components.menu.base.ContextMenuState.Companion.rememberContextMenuState
import org.mjdev.desktop.components.sliding.base.VisibilityState
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext

@Suppress("FunctionName")
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
    // the window is not focusable, so onFocusChange never fires after a click: the window manager
    // raised it over the bars on click and nothing sent it back, leaving the bars covered
    var desktopWindow by remember { mutableStateOf<java.awt.Window?>(null) }
    FullScreenWindow(
        name = "DesktopWindow",
        alwaysOnBottom = true,
        onOpened = {
            window?.toBack()
            window?.isFocusable = false
            desktopWindow = window
        },
        onFocusChange = { focus ->
            if (focus) {
                window?.toBack()
                window?.isFocusable = false
            }
        },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        // observe only (Initial pass, nothing consumed): desktop items still get the click
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.type == PointerEventType.Press) desktopWindow?.toBack()
                            }
                        }
                    },
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
fun PreviewDesktopWindow() = DesktopWindow()

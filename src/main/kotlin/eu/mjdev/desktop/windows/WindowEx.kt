package eu.mjdev.desktop.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState

@Composable
fun WindowEx(
    onCreate: (window: ComposeWindow) -> Unit = {},
//    onOpen: (window: ComposeWindow) -> Unit = {},
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(),
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    undecorated: Boolean = true,
    transparent: Boolean = true,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
//    alwaysOnBottom: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    focusHelper: WindowFocusHelper,
    stateHelper: WindowStateHelper,
    content: @Composable FrameWindowScope.() -> Unit = {}
) = Window(
    onCreate = onCreate,
//    onOpen = onOpen,
    onCloseRequest = onCloseRequest,
    state = state,
    visible = visible,
    title = title,
    icon = icon,
    undecorated = undecorated,
    transparent = transparent,
    resizable = resizable,
    enabled = enabled,
    focusable = focusable,
    alwaysOnTop = alwaysOnTop,
//    alwaysOnBottom = alwaysOnBottom,
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    content = {
        content()
        with(window) {
            stateHelper.register(this)
            focusHelper.register(this)
        }
    }
)

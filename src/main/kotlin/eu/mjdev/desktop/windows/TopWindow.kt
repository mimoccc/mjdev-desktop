package eu.mjdev.desktop.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState

@Composable
fun TopWindow(
    resizable: Boolean = false,
    title: String = "",
    icon: Painter? = null,
    focusable: Boolean = true,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    windowState: WindowState,
    onCloseRequest: () -> Unit = {},
    content: @Composable FrameWindowScope.() -> Unit
) = Window(
    state = windowState,
    resizable = resizable,
    undecorated = true,
    onCloseRequest = onCloseRequest,
    visible = true,
    transparent = true,
    title = title,
    icon = icon,
    focusable = focusable,
    alwaysOnTop = true,
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    content = content
)

package eu.mjdev.desktop.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState

@Composable
fun WindowEx(
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
    alwaysOnBottom: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    focusHelper: WindowFocusHelper,
    stateHelper: WindowStateHelper,
    content: @Composable FrameWindowScope.() -> Unit = {}
) = Window(
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
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    content = {
        content()
        with(window) {
            if (alwaysOnBottom) {
                toBack()
            } else if (alwaysOnTop) {
                toFront()
            }
            stateHelper.register(this)
            focusHelper.register(this)
        }
    }
)

package eu.mjdev.desktop.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@Composable
fun FullScreenWindow(
    visible: Boolean = true,
    title: String = "",
    icon: Painter? = null,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    api: DesktopProvider = LocalDesktop.current,
    onCloseRequest: () -> Unit = {},
    content: @Composable FrameWindowScope.() -> Unit
) = Window(
    state = rememberWindowState(
        placement = WindowPlacement.Floating,
        size = api.containerSize
    ),
    resizable = false,
    undecorated = true,
    transparent = true,
    focusable = focusable,
    alwaysOnTop = alwaysOnTop,
    visible = visible,
    title = title,
    icon = icon,
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    onCloseRequest = onCloseRequest,
    content = content
)
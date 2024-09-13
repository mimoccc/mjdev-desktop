package eu.mjdev.desktop.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.WindowPlacement
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@Composable
fun FullScreenWindow(
    api: DesktopProvider = LocalDesktop.current,
    onCloseRequest: () -> Unit = {},
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable () -> Unit = {}
) = ChromeWindow(
    size = api.containerSize,
    resizable = false,
    transparent = true,
    visible = true,
    focusable = true,
    alwaysOnTop = false,
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    onCloseRequest = onCloseRequest,
    placement = WindowPlacement.Fullscreen,
    content = content
)
package eu.mjdev.desktop.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Composable
fun FullScreenWindow(
    api: DesktopProvider = LocalDesktop.current,
    onCloseRequest: () -> Unit = {},
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    windowState: ChromeWindowState = rememberChromeWindowState(
        placement = WindowPlacement.Fullscreen,
        size = api.containerSize,
        position = WindowPosition.Aligned(Alignment.Center)
    ),
    content: @Composable ChromeWindowScope.() -> Unit = {}
) = ChromeWindow(
    windowState = windowState,
    resizable = false,
    transparent = true,
    visible = true,
    focusable = false,
    alwaysOnTop = false,
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    onCloseRequest = onCloseRequest,
    onFocusChange = {
    },
    content = content
)
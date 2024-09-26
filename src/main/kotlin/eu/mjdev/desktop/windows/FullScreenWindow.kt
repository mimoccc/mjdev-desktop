package eu.mjdev.desktop.windows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Suppress("FunctionName")
@Preview
@Composable
fun FullScreenWindow(
    api: DesktopProvider = LocalDesktop.current,
    enabled: Boolean = true,
    focusable: Boolean = true,
    onOpened: ChromeWindowState.() -> Unit = {},
    onCloseRequest: () -> Unit = {},
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    windowState: ChromeWindowState = rememberChromeWindowState(
        placement = WindowPlacement.Maximized,
        size = api.containerSize,
        position = WindowPosition.Aligned(Alignment.Center)
    ),
    onCreate: ChromeWindowState.() -> Unit = {},
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {
    },
    content: @Composable () -> Unit = {}
) = ChromeWindow(
    windowState = windowState,
    enabled = enabled,
    resizable = false,
    transparent = true,
    focusable = focusable,
    visible = true,
    alwaysOnTop = false,
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    onCloseRequest = onCloseRequest,
    onCreate = onCreate,
    onOpened = onOpened,
    onFocusChange = onFocusChange,
    content = content
)
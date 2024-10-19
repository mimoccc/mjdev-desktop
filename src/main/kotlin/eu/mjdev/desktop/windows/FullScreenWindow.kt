package eu.mjdev.desktop.windows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Suppress("FunctionName")
@Preview
@Composable
fun FullScreenWindow(
    name: String? = null,
    api: DesktopProvider = LocalDesktop.current,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnBottom: Boolean = false,
    onOpened: ChromeWindowState.() -> Unit = {},
    onCloseRequest: () -> Unit = {},
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    windowState: ChromeWindowState = rememberChromeWindowState(
        placement = WindowPlacement.Maximized,
        size = api.containerSize,
        position = WindowPosition.Aligned(Alignment.Center)
    ),
    onCreated: ChromeWindowState.() -> Unit = {},
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    content: @Composable () -> Unit = {}
) = withDesktopScope {
    ChromeWindow(
        name = name ?: "FullScreenWindow",
        visible = true,
        enabled = enabled,
        resizable = false,
        transparent = true,
        focusable = focusable,
        alwaysOnTop = false,
        alwaysOnBottom = alwaysOnBottom,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        onCloseRequest = onCloseRequest,
        onCreated = onCreated,
        onOpened = onOpened,
        onFocusChange = onFocusChange,
        windowState = windowState,
        content = content
    )
}

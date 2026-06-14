package org.mjdev.desktop.windows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpOffset
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.context.LocalDesktopContext
import org.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Suppress("FunctionName")
@Composable
fun FullScreenWindow(
    name: String? = null,
    context: IDesktopContext = LocalDesktopContext.current,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnBottom: Boolean = false,
    onOpened: ChromeWindowState.() -> Unit = {},
    onCloseRequest: () -> Unit = {},
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    windowState: ChromeWindowState =
        rememberChromeWindowState(
            placement = WindowPlacement.Fullscreen,
            size = context.containerSize,
            position = DpOffset.Zero,
        ),
    onCreated: ChromeWindowState.() -> Unit = {},
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    content: @Composable () -> Unit = {},
) = withDesktopContext {
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
        content = content,
    )
}

// todo Preview

package org.mjdev.desktop.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.isDesign
import org.mjdev.desktop.helpers.keyevents.GlobalKeyListener.Companion.globalKeyEventHandler
import org.mjdev.desktop.helpers.keyevents.KeyEventHandler
import org.mjdev.desktop.helpers.mouseevents.GlobalMouseListener.Companion.globalMouseEventHandler
import org.mjdev.desktop.helpers.mouseevents.MouseEventHandler
import org.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import org.mjdev.desktop.windows.ChromeWindowState.Companion.rememberWnState
import org.mjdev.desktop.windows.ChromeWindowState.Companion.updateWindowState

@Suppress("FunctionName")
@Composable
fun ChromeWindow(
    name: String? = null,
    visible: Boolean = true,
    transparent: Boolean = true,
    resizable: Boolean = false,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    alwaysOnBottom: Boolean = false,
    position: DpOffset = DpOffset.Zero,
    size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified),
    onCloseRequest: () -> Unit = {},
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onCreated: ChromeWindowState.() -> Unit = {},
    onOpened: ChromeWindowState.() -> Unit = {},
    onClosed: ChromeWindowState.() -> Unit = {},
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    windowState: ChromeWindowState =
        rememberChromeWindowState(
            visible = visible,
            size = size,
            position = position,
        ),
    isGlobalKeyHandlerEnabled: () -> Boolean = { false },
    onGlobalKey: KeyEventHandler.() -> Unit = {},
    isGlobalMouseHandlerEnabled: () -> Boolean = { false },
    onGlobalMouse: MouseEventHandler.() -> Unit = {},
    content: @Composable () -> Unit = {},
) = withDesktopContext {
    globalKeyEventHandler(
        isEnabled = isGlobalKeyHandlerEnabled,
        block = onGlobalKey,
    )
    globalMouseEventHandler(
        enabled = isGlobalMouseHandlerEnabled,
        block = onGlobalMouse,
    )
    if (isDesign) {
        content()
    } else {
        val wnState = rememberWnState()
        windowState.onOpened {
            onOpened(windowState)
        }
        windowState.onClosed(onClosed)
        windowState.onFocusChange(onFocusChange)
        Window(
            name = name,
            onCreated = { onCreated(windowState) },
            onOpened = { onOpened(windowState) },
            onClosed = { onClosed(windowState) },
            onCloseRequest = onCloseRequest,
            state = windowState,
            visible = visible,
            title = "",
            icon = null,
            undecorated = true,
            transparent = transparent,
            resizable = resizable,
            enabled = enabled,
            focusable = focusable,
            alwaysOnTop = alwaysOnTop,
            alwaysOnBottom = alwaysOnBottom,
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
            stateHelper = windowState.stateHelper,
            focusHelper = windowState.focusHelper,
            content = {
                content()
            },
        )
        updateWindowState(visible, wnState, windowState)
    }
}

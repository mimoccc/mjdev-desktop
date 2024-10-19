package eu.mjdev.desktop.windows

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.extensions.Compose.isDesign
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberAnimState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberWnState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.updateAnimState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.updateWindowState

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
    position: WindowPosition = WindowPosition.Aligned(Alignment.Center),
    size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified),
    enterAnimation: EnterTransition = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exitAnimation: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    onCloseRequest: () -> Unit = {},
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onCreated: ChromeWindowState.() -> Unit = {},
    onOpened: ChromeWindowState.() -> Unit = {},
    onClosed: ChromeWindowState.() -> Unit = {},
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    windowState: ChromeWindowState = rememberChromeWindowState(
        visible = visible,
        size = size,
        position = position
    ),
    content: @Composable () -> Unit = {}
) = withDesktopScope {
    if (isDesign) {
        content()
    } else {
        val animState = rememberAnimState(visible)
        val wnState = rememberWnState()
        windowState.onOpened {
            if (visible) {
                animState.value.targetState = true
            }
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
            visible = wnState.value,
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
                AnimatedVisibility(
                    animState.value,
                    enter = enterAnimation,
                    exit = exitAnimation
                ) {
                    content()
                }
            }
        )
        updateAnimState(visible, animState, wnState)
        updateWindowState(visible, animState, wnState, windowState)
    }
}
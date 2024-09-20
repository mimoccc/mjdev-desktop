package eu.mjdev.desktop.windows

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.windows.ChromeWindowScope.Companion.withChromeWindowScope
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberAnimState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberWnState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.updateAnimState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.updateWindowState

@Suppress("FunctionName", "unused")
@Composable
fun ChromeWindow(
    size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified),
    position: WindowPosition = WindowPosition.Aligned(Alignment.Center),
    windowState: ChromeWindowState = rememberChromeWindowState(position = position, size = size),
    visible: Boolean = true,
    transparent: Boolean = true,
    resizable: Boolean = false,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = true,
//    alwaysOnBottom: Boolean = false,
    enterAnimation: EnterTransition = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exitAnimation: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    onCreate: ChromeWindowState.() -> Unit = {},
    onCloseRequest: () -> Unit = {},
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    onOpened: ChromeWindowState.() -> Unit = {},
    onClosed: ChromeWindowState.() -> Unit = {},
    content: @Composable ChromeWindowScope.() -> Unit = {}
) = ChromeWindow(
    windowState = windowState,
    visible = visible,
    transparent = transparent,
    resizable = resizable,
    enabled = enabled,
    focusable = focusable,
    alwaysOnTop = alwaysOnTop,
//    alwaysOnBottom = alwaysOnBottom,
    enterAnimation = enterAnimation,
    exitAnimation = exitAnimation,
    onCreate = onCreate,
    onCloseRequest = onCloseRequest,
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    onFocusChange = onFocusChange,
    onOpened = onOpened,
    onClosed = onClosed,
    content = content
)

@Suppress("unused", "FunctionName")
@Composable
fun ChromeWindow(
    position: WindowPosition = WindowPosition.Aligned(Alignment.Center),
    windowState: ChromeWindowState = rememberChromeWindowState(position = position),
    visible: Boolean = true,
    transparent: Boolean = true,
    resizable: Boolean = false,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = true,
//    alwaysOnBottom: Boolean = false,
    enterAnimation: EnterTransition = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exitAnimation: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    onCreate: ChromeWindowState.() -> Unit = {},
    onCloseRequest: () -> Unit = {},
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    onOpened: ChromeWindowState.() -> Unit = {},
    onClosed: ChromeWindowState.() -> Unit = {},
    content: @Composable ChromeWindowScope.() -> Unit = {}
) = ChromeWindow(
    windowState = windowState,
    visible = visible,
    transparent = transparent,
    resizable = resizable,
    enabled = enabled,
    focusable = focusable,
    alwaysOnTop = alwaysOnTop,
//    alwaysOnBottom = alwaysOnBottom,
    enterAnimation = enterAnimation,
    exitAnimation = exitAnimation,
    onCreate = onCreate,
    onCloseRequest = onCloseRequest,
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    onFocusChange = onFocusChange,
    onOpened = onOpened,
    onClosed = onClosed,
    content = content
)

@Suppress("FunctionName")
@Composable
fun ChromeWindow(
    windowState: ChromeWindowState = rememberChromeWindowState(),
    visible: Boolean = true,
    transparent: Boolean = true,
    resizable: Boolean = false,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = true,
//    alwaysOnBottom: Boolean = false,
    enterAnimation: EnterTransition = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exitAnimation: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    onCloseRequest: () -> Unit = {},
    onCreate: ChromeWindowState.() -> Unit = {},
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    onOpened: ChromeWindowState.() -> Unit = {},
    onClosed: ChromeWindowState.() -> Unit = {},
    content: @Composable ChromeWindowScope.() -> Unit = {}
) {
    val animState = rememberAnimState(visible)
    val wnState = rememberWnState()
    windowState.onOpened.add {
//        if (alwaysOnBottom) {
            // todo
//            val focusOwner = window?.mostRecentFocusOwner
//            window?.toBack()
//            focusOwner?.requestFocus()
//        }
//        else if (alwaysOnTop) {
//            windowState.requestFocus()
//        }
        windowState.updatePositionAndSize()
        if (visible) {
            animState.value.targetState = true
        }
        onOpened(windowState)
    }
    windowState.onClosed.add(onClosed)
    windowState.onFocusChange.add(onFocusChange)
    WindowEx(
        onCreate = { window ->
            windowState.window = window
            onCreate(windowState)
        },
//        onOpen = {
//            onOpened(windowState)
//        },
        onCloseRequest,
        windowState,
        wnState.value,
        "",
        null,
        true,
        transparent,
        resizable,
        enabled,
        focusable,
        alwaysOnTop,
//        alwaysOnBottom,
        onPreviewKeyEvent,
        onKeyEvent,
        stateHelper = windowState.stateHelper,
        focusHelper = windowState.focusHelper,
        content = {
            withChromeWindowScope(
                windowState,
                animState,
                wnState
            ) {
                AnimatedVisibility(
                    animState.value,
                    enter = enterAnimation,
                    exit = exitAnimation
                ) {
                    content()
                }
            }
        }
    )
    updateAnimState(visible, animState, wnState)
    updateWindowState(visible, animState, wnState, windowState)
}

package eu.mjdev.desktop.windows

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberAnimState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberWnState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.updateAnimState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.updateWindowState

@Composable
fun ChromeWindow(
    position: WindowPosition = WindowPosition.Aligned(Alignment.Center),
    size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified),
    visible: Boolean = true,
    transparent: Boolean = true,
    resizable: Boolean = false,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = true,
    placement: WindowPlacement = WindowPlacement.Floating,
    windowState: ChromeWindowState = rememberChromeWindowState(position, size, placement),
    enterAnimation: EnterTransition = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exitAnimation: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    onCloseRequest: () -> Unit = {},
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onUpdate: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val animState = rememberAnimState(visible)
    val wnState = rememberWnState()
    WindowEx(
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
        onPreviewKeyEvent,
        onKeyEvent,
        {
            if (visible) {
                animState.value.targetState = true
            }
            onUpdate()
        },
        {
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
    updateWindowState(visible, animState, wnState)
}

class ChromeWindowState(
    position: WindowPosition = WindowPosition.Aligned(Alignment.Center),
    size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified),
    placement: WindowPlacement = WindowPlacement.Floating,
) : WindowState {

    override var position: WindowPosition = position
    override var size: DpSize = size
    override var placement: WindowPlacement = placement
    override var isMinimized: Boolean = false

    companion object {

        @Composable
        fun rememberChromeWindowState(
            position: WindowPosition = WindowPosition.Aligned(Alignment.Center),
            size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified),
            placement: WindowPlacement = WindowPlacement.Floating,
        ) = remember(position, size, placement) {
            ChromeWindowState(position, size, placement)
        }

        @Composable
        fun rememberAnimState(visible: Boolean) = remember(visible) {
            derivedStateOf {
                when (visible) {
                    true -> MutableTransitionState(false)
                    false -> MutableTransitionState(true)
                }
            }
        }

        @Composable
        fun rememberWnState() = remember { mutableStateOf(false) }

        @Composable
        fun updateWindowState(
            visible: Boolean,
            animState: State<MutableTransitionState<Boolean>>,
            wnState: MutableState<Boolean>
        ) = launchedEffect(animState.value.currentState) { currentState ->
            if (animState.value.targetState == currentState && !visible) {
                wnState.value = false
            }
        }

        @Composable
        fun updateAnimState(
            visible: Boolean,
            animState: State<MutableTransitionState<Boolean>>,
            wnState: MutableState<Boolean>
        ) = launchedEffect(visible) {
            if (visible) {
                wnState.value = true
            } else {
                animState.value.targetState = false
            }
        }

    }

}

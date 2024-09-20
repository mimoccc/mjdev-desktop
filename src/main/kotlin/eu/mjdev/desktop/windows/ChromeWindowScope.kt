package eu.mjdev.desktop.windows

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope

@Suppress("unused", "MemberVisibilityCanBePrivate")
class ChromeWindowScope(
    val frameScope: FrameWindowScope,
    val windowState: ChromeWindowState,
    val animState: State<MutableTransitionState<Boolean>>,
    val windowVisibleState: MutableState<Boolean>
) {
    val window
        get() = windowState.window

    val stateHelper: WindowStateHelper
        get() = windowState.stateHelper

    val focusHelper: WindowFocusHelper
        get() = windowState.focusHelper

    val x : Dp
        get() = frameScope.window.x.dp

    val y : Dp
        get() = frameScope.window.y.dp

    val width : Dp
        get() = frameScope.window.width.dp

    val height : Dp
        get() = frameScope.window.height.dp

    fun apply(
        block: ChromeWindowScope.() -> Unit
    ) = with(this) {
        block()
    }

    fun requestFocus() = {
        windowState.requestFocus()
    }

    companion object {
        @Composable
        fun FrameWindowScope.withChromeWindowScope(
            windowState: ChromeWindowState,
            animState: State<MutableTransitionState<Boolean>>,
            windowVisibleState: MutableState<Boolean>,
            block: @Composable ChromeWindowScope.() -> Unit
        ) = with(ChromeWindowScope(windowState, animState, windowVisibleState)) {
            block()
        }
    }
}
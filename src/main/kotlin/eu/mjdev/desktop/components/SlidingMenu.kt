package eu.mjdev.desktop.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import eu.mjdev.desktop.components.SlideMenuState.Companion.rememberSlideMenuState
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.LocalDesktop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("FunctionName")
@Preview
@Composable
fun SlidingMenu(
    modifier: Modifier = Modifier,
    state: SlideMenuState = rememberSlideMenuState(),
    orientation: Orientation = Horizontal,
    onVisibilityChange: (visible: Boolean) -> Unit = {},
    api: DesktopProvider = LocalDesktop.current,
    content: @Composable (isVisible: Boolean) -> Unit
) {
    when (orientation) {
        Horizontal -> Box(
            modifier = modifier
                .fillMaxHeight()
                .wrapContentWidth()
                .onPointerEvent(PointerEventType.Enter) {
                    if (api.windowFocusState.isFocused) {
                        state.show()
                    }
                }
        ) {
            content(state.isVisible)
        }

        Vertical -> Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .onPointerEvent(PointerEventType.Enter) {
                    if (api.windowFocusState.isFocused) {
                        state.show()
                    }
                }
        ) {
            content(state.isVisible)
        }
    }
    launchedEffect(state.isVisible) {
        onVisibilityChange(state.isVisible)
    }
//    launchedEffect(windowFocusState.isFocused) {
//        if (!windowFocusState.isFocused) {
//            state.hide()
//        }
//    }
}

@Suppress("CanBeParameter")
class SlideMenuState(
    private val scope: CoroutineScope,
    private val startState: Boolean = false,
    private val enabled: Boolean = true
) {
    var isVisible: Boolean
        get() = visible.value
        set(value) {
            visible.value = value
        }

    private val visible: MutableState<Boolean> = mutableStateOf(startState)

    fun show() {
        if (enabled) scope.launch {
            visible.value = true
        }
    }

    fun hide(delay: Long = 0L) {
        if (enabled) scope.launch {
            delay(delay)
            visible.value = false
        }
    }

    fun toggle() {
        visible.value = !visible.value
    }

    companion object {
        @Composable
        fun rememberSlideMenuState(
            startState: Boolean = false,
            enabled: Boolean = true
        ): SlideMenuState {
            val scope = rememberCoroutineScope()
            return remember(startState, enabled) {
                SlideMenuState(scope, startState, enabled)
            }
        }
    }
}

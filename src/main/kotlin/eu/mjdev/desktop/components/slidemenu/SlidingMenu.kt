package eu.mjdev.desktop.components.slidemenu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import eu.mjdev.dadb.helpers.log
import eu.mjdev.desktop.components.slidemenu.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.launchedEffect

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("FunctionName")
@Preview
@Composable
fun SlidingMenu(
    modifier: Modifier = Modifier,
    state: VisibilityState = rememberVisibilityState(),
    orientation: Orientation = Horizontal,
    onVisibilityChange: (visible: Boolean) -> Unit = {},
    content: @Composable (isVisible: Boolean) -> Unit
) {
    when (orientation) {
        Horizontal -> Box(
            modifier = modifier
                .fillMaxHeight()
                .wrapContentWidth()
                .onPointerEvent(PointerEventType.Enter) {
                    log { "Showing sliding menu." }
                    state.show()
                }
        ) {
            content(state.isVisible)
        }

        Vertical -> Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .onPointerEvent(PointerEventType.Enter) {
                    state.show()
                }
        ) {
            content(state.isVisible)
        }
    }
    launchedEffect(state.isVisible) {
        onVisibilityChange(state.isVisible)
    }
}

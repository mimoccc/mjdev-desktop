package eu.mjdev.desktop.components.sliding

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("FunctionName")
@Composable
fun SlidingMenu(
    modifier: Modifier = Modifier,
    state: VisibilityState = rememberVisibilityState(),
    orientation: Orientation = Horizontal,
    onPointerEnter: () -> Unit = {
        state.show()
    },
    onPointerLeave: () -> Unit = {
//        state.hide()
    },
    content: @Composable BoxScope.(isVisible: Boolean) -> Unit = {}
) {
    when (orientation) {
        Horizontal -> Box(
            modifier = modifier
                .fillMaxHeight()
                .onPointerEvent(PointerEventType.Enter) { onPointerEnter() }
                .onPointerEvent(PointerEventType.Exit) { onPointerLeave() }
        ) {
            content(state.isVisible)
        }

        Vertical -> Box(
            modifier = modifier
                .fillMaxWidth()
                .onPointerEvent(PointerEventType.Enter) { onPointerEnter() }
                .onPointerEvent(PointerEventType.Exit) { onPointerLeave() }
        ) {
            content(state.isVisible)
        }
    }
}

@Preview
@Composable
fun SlidingMenuPreview() = SlidingMenu()

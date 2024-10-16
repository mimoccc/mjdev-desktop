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
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.onMouseEnter
import eu.mjdev.desktop.extensions.Compose.onMouseLeave
import eu.mjdev.desktop.extensions.Compose.preview

// todo remove, move to chrome window
@Suppress("FunctionName")
@Composable
fun SlidingPanel(
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
                .onMouseEnter { onPointerEnter() }
                .onMouseLeave { onPointerLeave() }
        ) {
            content(state.isVisible)
        }

        Vertical -> Box(
            modifier = modifier
                .fillMaxWidth()
                .onMouseEnter { onPointerEnter() }
                .onMouseLeave { onPointerLeave() }
        ) {
            content(state.isVisible)
        }
    }
}

@Preview
@Composable
fun SlidingMenuPreview() = preview {
    SlidingPanel()
}

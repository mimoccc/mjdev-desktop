package org.psippr.desktop.components.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

// todo
@Composable
fun CursorDropdownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    // top-left corner of the menu, in pixels of the surface it is drawn on
    position: Offset = Offset.Zero,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    // a press outside the open menu closes it (old: onDismissRequest was never called)
    if (expanded) MenuDismissLayer(onDismiss = onDismissRequest)
    AnimatedVisibility(
        visible = expanded,
        // old: drawn at the top-left corner, as wide as the window
//    modifier = modifier,
        modifier =
            Modifier
                .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                .then(modifier)
                .width(IntrinsicSize.Max),
    ) {
        // old: the items were laid over each other
//    content()
        Column {
            content()
        }
    }
}

// todo preview

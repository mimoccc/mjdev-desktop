package eu.mjdev.desktop.components.draggable

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.extensions.Modifier.conditional

@Suppress("FunctionName")
@Composable
fun DraggableView(
    modifier: Modifier = Modifier,
    dragEnabled: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    dragBackgroundColor: Color = Color.White.copy(alpha = 0.3f),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    zIndex: Float = Float.MIN_VALUE,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onTap: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    onClick: () -> Unit = {},
    onContextMenuClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit = {}
) {
    var position by rememberState(Offset.Zero)
    var isDragging by rememberState(false)
    Box(
        modifier = modifier.wrapContentSize()
            .conditional(zIndex > Float.MIN_VALUE) {
                zIndex(zIndex)
            }
            .graphicsLayer(
                translationX = position.x,
                translationY = position.y
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onPress = { onClick() },
                    onDoubleTap = { onDoubleTap() },
                    onLongPress = { onContextMenuClick() }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        if (dragEnabled) {
                            change.consume()
                            position += dragAmount
                        }
                    },
                    onDragStart = {
                        isDragging = true
                        onDragStart()
                    },
                    onDragEnd = {
                        isDragging = false
                        onDragEnd()
                    }
                )
            }.padding(contentPadding),
        content = {
            Box(
                modifier = Modifier
                    .background(if (isDragging) dragBackgroundColor else backgroundColor)
            ) {
                content()
            }
        }
    )
}

@Preview
@Composable
fun DraggableViewPreview() = preview {
    DraggableView()
}

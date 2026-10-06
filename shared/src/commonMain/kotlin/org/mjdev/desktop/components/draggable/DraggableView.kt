package org.mjdev.desktop.components.draggable

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Modifier.conditional
import org.mjdev.desktop.extensions.MutableStateExt.rememberState

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
    // saved offset to start from (e.g. restored after relaunch)
    initialPosition: Offset = Offset.Zero,
    // called with the new offset when a drag ends, to save it
    onPositionChange: (Offset) -> Unit = {},
    content: @Composable BoxScope.() -> Unit = {},
) {
    var position by rememberState(initialPosition)
    var isDragging by rememberState(false)
    var isPlaced by rememberState(false)
    Box(
        modifier =
            modifier
                .wrapContentSize()
                // before graphicsLayer: coordinates without the drag offset; on first placement
                // a restored offset is pulled back inside the parent (e.g. smaller screen now)
                .onPlaced { coordinates ->
                    if (!isPlaced) {
                        isPlaced = true
                        coordinates.parentLayoutCoordinates?.let { parent ->
                            position =
                                DragBounds.clamp(
                                    offset = position,
                                    origin = coordinates.positionInParent(),
                                    size = coordinates.size,
                                    parentSize = parent.size,
                                )
                        }
                    }
                }.conditional(zIndex > Float.MIN_VALUE) {
                    zIndex(zIndex)
                }.graphicsLayer(
                    translationX = position.x,
                    translationY = position.y,
                ).pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onPress = { onClick() },
                        onDoubleTap = { onDoubleTap() },
                        onLongPress = { onContextMenuClick() },
                    )
                }.pointerInput(Unit) {
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
                            onPositionChange(position)
                            onDragEnd()
                        },
                    )
                }.padding(contentPadding),
        content = {
            Box(
                modifier =
                    Modifier
                        .background(if (isDragging) dragBackgroundColor else backgroundColor),
            ) {
                content()
            }
        },
    )
}

@Preview
@Composable
fun PreviewDraggableView() = preview {
    DraggableView()
}

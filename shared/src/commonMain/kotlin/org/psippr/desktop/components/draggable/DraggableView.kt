package org.psippr.desktop.components.draggable

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.Modifier.conditional
import org.psippr.desktop.extensions.MutableStateExt.rememberState

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
    val currentOnDoubleTap by rememberUpdatedState(onDoubleTap)
    val doubleClick =
        remember {
            DoubleClickDetector(timeoutMillis = DOUBLE_CLICK_TIMEOUT_MS, slop = DOUBLE_CLICK_SLOP_PX)
        }
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
                    // old: the double tap timed out on a busy UI thread, see DoubleClickDetector
//                    detectTapGestures(
//                        onTap = { onTap() },
//                        onPress = { onClick() },
//                        onDoubleTap = { onDoubleTap() },
//                        onLongPress = { onContextMenuClick() },
//                    )
                    detectTapGestures(
                        onTap = { onTap() },
                        onPress = { onClick() },
                        onLongPress = { onContextMenuClick() },
                    )
                }.pointerInput(Unit) {
                    // observed only (nothing consumed): taps and drags above still get the events
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull() ?: continue
                            when (event.type) {
                                // left button only; a release without its press is ignored
                                PointerEventType.Press ->
                                    if (event.buttons.isPrimaryPressed) doubleClick.onPress(change.position)
                                PointerEventType.Release ->
                                    if (doubleClick.onRelease(
                                            change.uptimeMillis,
                                            change.position,
                                        )
                                    ) {
                                        currentOnDoubleTap()
                                    }
                            }
                        }
                    }
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

/** Longest time between the clicks of a double click (the usual desktop default). */
private const val DOUBLE_CLICK_TIMEOUT_MS = 400L

/** Longest move of a click, and between the two clicks of a double click, in pixels. */
private const val DOUBLE_CLICK_SLOP_PX = 8f

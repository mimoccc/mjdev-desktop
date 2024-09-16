package eu.mjdev.desktop.components.draggable

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun DraggableView(
    modifier: Modifier = Modifier,
    dragEnabled: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    dragBackgroundColor: Color = Color.White.copy(alpha = 0.3f),
    content: @Composable BoxScope.() -> Unit = {}
) {
    var position by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .graphicsLayer(
                translationX = position.x,
                translationY = position.y
            )
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
                    },
                    onDragEnd = {
                        isDragging = false
                    }
                )
            },
        content = {
            Box(
                modifier = Modifier.background(if (isDragging) dragBackgroundColor else backgroundColor)
            ) {
                content()
            }
        }
    )
}

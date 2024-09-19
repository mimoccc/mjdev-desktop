package eu.mjdev.desktop.components.draggable

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Compose.rememberState

@Composable
fun DraggableView(
    modifier: Modifier = Modifier,
    dragEnabled: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    dragBackgroundColor: Color = Color.White.copy(alpha = 0.3f),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit = {}
) {
    var position by rememberState(Offset.Zero)
    var isDragging by rememberState(false)
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

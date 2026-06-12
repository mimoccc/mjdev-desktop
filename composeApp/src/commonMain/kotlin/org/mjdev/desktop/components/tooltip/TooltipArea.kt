package org.mjdev.desktop.components.tooltip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.areAnyPressed
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mjdev.desktop.extensions.Modifier.onPointerEvent
import org.jetbrains.compose.ui.tooling.preview.Preview

val PointerEvent.position get() = changes.first().position

@Composable
fun TooltipArea(
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Int = 500,
    tooltipPlacement: TooltipPlacement = TooltipPlacement.ComponentRect(
        offset = DpOffset(0.dp, 16.dp)
    ),
    content: @Composable BoxScope.() -> Unit
) {
    var parentBounds by remember { mutableStateOf(Rect.Zero) }
    var cursorPosition by remember { mutableStateOf(Offset.Zero) }
    var isVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var job: Job? by remember { mutableStateOf(null) }

    fun startShowing() {
        if (job?.isActive == true) return
        job = scope.launch {
            delay(delayMillis.toLong())
            isVisible = true
        }
    }

    fun hide() {
        job?.cancel()
        job = null
        isVisible = false
    }

    fun hideIfNotHovered(globalPosition: Offset) {
        if (!parentBounds.contains(globalPosition)) {
            hide()
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { parentBounds = it.boundsInWindow() }
            .onPointerEvent(PointerEventType.Enter) {
                cursorPosition = it.position
                if (!isVisible && !it.buttons.areAnyPressed) {
                    startShowing()
                }
            }
            .onPointerEvent(PointerEventType.Move) {
                cursorPosition = it.position
                if (!isVisible && !it.buttons.areAnyPressed) {
                    startShowing()
                }
            }
            .onPointerEvent(PointerEventType.Exit) {
                hideIfNotHovered(parentBounds.topLeft + it.position)
            }
            .onPointerEvent(PointerEventType.Press, pass = PointerEventPass.Initial) {
                hide()
            }
    ) {
        content()
        if (isVisible) {
            Popup(
                popupPositionProvider = tooltipPlacement.positionProvider(cursorPosition),
                onDismissRequest = { isVisible = false }
            ) {
                var popupPosition by remember { mutableStateOf(Offset.Zero) }
                Box(
                    Modifier
                        .onGloballyPositioned { popupPosition = it.positionInWindow() }
                        .onPointerEvent(PointerEventType.Move) {
                            hideIfNotHovered(popupPosition + it.position)
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            hideIfNotHovered(popupPosition + it.position)
                        }
                ) {
                    tooltip()
                }
            }
        }
    }
}

// todo preview

package org.psippr.desktop.components.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Invisible layer over the whole surface, under an open menu: the next press anywhere outside
 * the menu closes it ([onDismiss]) and goes no further, like a click outside any menu.
 */
@Suppress("FunctionName")
@Composable
fun MenuDismissLayer(onDismiss: () -> Unit) {
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press) {
                                event.changes.forEach { change -> change.consume() }
                                currentOnDismiss()
                            }
                        }
                    }
                },
    )
}

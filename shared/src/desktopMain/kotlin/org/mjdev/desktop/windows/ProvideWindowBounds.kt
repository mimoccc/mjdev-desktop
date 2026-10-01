package org.mjdev.desktop.windows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.blur.LocalWindowBounds
import org.mjdev.desktop.components.blur.WindowBounds
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

/**
 * Provides the live screen bounds of [window] to [content] through [LocalWindowBounds]. The bounds
 * come straight from AWT, so they stay right when the compositor or the user moves the window.
 */
@Composable
fun ProvideWindowBounds(
    window: Window,
    content: @Composable () -> Unit,
) {
    var bounds by remember(window) { mutableStateOf(window.currentBounds()) }
    DisposableEffect(window) {
        val listener =
            object : ComponentAdapter() {
                override fun componentMoved(e: ComponentEvent) {
                    bounds = window.currentBounds()
                }

                override fun componentResized(e: ComponentEvent) {
                    bounds = window.currentBounds()
                }

                override fun componentShown(e: ComponentEvent) {
                    bounds = window.currentBounds()
                }
            }
        window.addComponentListener(listener)
        bounds = window.currentBounds()
        onDispose { window.removeComponentListener(listener) }
    }
    CompositionLocalProvider(LocalWindowBounds provides bounds, content = content)
}

private fun Window.currentBounds() = WindowBounds(
    position = DpOffset(x.dp, y.dp),
    size = DpSize(width.dp, height.dp),
)

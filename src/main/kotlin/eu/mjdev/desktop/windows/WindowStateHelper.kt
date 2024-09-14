package eu.mjdev.desktop.windows

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import java.awt.event.WindowEvent
import java.awt.event.WindowEvent.WINDOW_CLOSED
import java.awt.event.WindowEvent.WINDOW_OPENED
import java.awt.event.WindowStateListener

class WindowStateHelper(
    val onOpened: WindowStateHelper.(window: ComposeWindow?) -> Unit = {},
    val onClosed: WindowStateHelper.(window: ComposeWindow?) -> Unit = {},
) : WindowStateListener {

    val x
        get() = (window?.x ?: 0).dp

    val y
        get() = (window?.y ?: 0).dp

    val width
        get() = (window?.width ?: 0).dp

    val height
        get() = (window?.height ?: 0).dp

    var window: ComposeWindow? = null

    fun register(window: ComposeWindow) {
        this.window = window
        window.addWindowStateListener(this)
        onOpened(window)
    }

    fun unregister(window: ComposeWindow) {
        window.removeWindowStateListener(this)
    }

    fun setBounds(
        x: Dp = this.x,
        y: Dp = this.y,
        width: Dp = this.width,
        height: Dp = this.height
    ) {
        window?.setBounds(
            x.value.toInt(),
            y.value.toInt(),
            width.value.toInt(),
            height.value.toInt()
        )
    }

    fun setOffset(offset: DpOffset) = setBounds(x = offset.x, y = offset.y)

    fun setSize(size: DpSize) = setBounds(width = size.width, height = size.height)

    override fun windowStateChanged(e: WindowEvent?) {
        when (e?.newState) {
            null -> Unit
            WINDOW_OPENED -> {
                onOpened(window)
            }

            WINDOW_CLOSED -> {
                onClosed(window)
                window?.let { unregister(it) }
            }
        }
    }

    fun setPosition(position: WindowPosition) {
        when (position) {
            is WindowPosition.Absolute -> setOffset(position.let { DpOffset(it.x, it.y) })
            else -> {
                // todo
            }
        }
    }
}
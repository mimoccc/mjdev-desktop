package eu.mjdev.desktop.windows

import java.awt.Window
import java.awt.event.WindowEvent
import java.awt.event.WindowEvent.WINDOW_CLOSED
import java.awt.event.WindowEvent.WINDOW_OPENED
import java.awt.event.WindowStateListener

// todo more listeners and improved type
@Suppress("MemberVisibilityCanBePrivate")
class WindowStateListener(
    val onOpened: eu.mjdev.desktop.windows.WindowStateListener.(window: Window) -> Unit = {},
    val onClosed: eu.mjdev.desktop.windows.WindowStateListener.(window: Window) -> Unit = {},
) : WindowStateListener {

    fun register(window: Window) {
        window.addWindowStateListener(this)
        onOpened(window)
    }

    fun unregister(window: Window) {
        window.removeWindowStateListener(this)
    }

    override fun windowStateChanged(e: WindowEvent?) {
        when (e?.newState) {
            null -> Unit
            WINDOW_OPENED -> {
                onOpened(e.window)
            }

            WINDOW_CLOSED -> {
                onClosed(e.window)
                e.window?.let { unregister(it) }
            }
        }
    }
}
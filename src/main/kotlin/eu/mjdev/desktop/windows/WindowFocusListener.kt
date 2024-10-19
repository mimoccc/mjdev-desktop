package eu.mjdev.desktop.windows

import java.awt.Window
import java.awt.event.WindowEvent
import java.awt.event.WindowEvent.*
import java.awt.event.WindowFocusListener
import java.awt.event.WindowStateListener

@Suppress("MemberVisibilityCanBePrivate", "unused")
class WindowFocusListener(
    val onFocusChange: (window: Window, focus: Boolean) -> Unit
) : WindowFocusListener, WindowStateListener {

    fun register(window: Window) {
        window.addWindowFocusListener(this)
        window.addWindowStateListener(this)
    }

    fun unregister(window: Window) {
        window.removeWindowFocusListener(this)
        window.removeWindowStateListener(this)
    }

    override fun windowGainedFocus(e: WindowEvent) {
        onFocusChange(e.window, true)
    }

    override fun windowLostFocus(e: WindowEvent) {
        onFocusChange(e.window, false)
    }

    override fun windowStateChanged(e: WindowEvent) {
        when (e.newState) {
            WINDOW_CLOSED -> {
                onFocusChange(e.window, false)
                unregister(e.window)
            }

            WINDOW_GAINED_FOCUS -> onFocusChange(e.window, true)
            WINDOW_ICONIFIED -> onFocusChange(e.window, false)
            WINDOW_DEICONIFIED -> onFocusChange(e.window, true)
            WINDOW_ACTIVATED -> onFocusChange(e.window, true)
            WINDOW_DEACTIVATED -> onFocusChange(e.window, false)
        }
    }
}
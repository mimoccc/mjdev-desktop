package eu.mjdev.desktop.windows

import androidx.compose.ui.awt.ComposeWindow
import java.awt.event.WindowEvent
import java.awt.event.WindowEvent.*
import java.awt.event.WindowFocusListener
import java.awt.event.WindowStateListener

@Suppress("MemberVisibilityCanBePrivate", "unused")
class WindowFocusHelper(
    val onFocusChange: WindowFocusHelper.(window: ComposeWindow?, focus: Boolean) -> Unit
) : WindowFocusListener, WindowStateListener {

    var window: ComposeWindow? = null

    fun register(window: ComposeWindow) {
        this.window = window
        window.addWindowFocusListener(this)
        window.addWindowStateListener(this)
    }

    fun unregister(window: ComposeWindow) {
        window.removeWindowFocusListener(this)
        window.removeWindowStateListener(this)
    }

    override fun windowGainedFocus(e: WindowEvent?) {
        onFocusChange(window, true)
    }

    override fun windowLostFocus(e: WindowEvent?) {
        onFocusChange(window, false)
    }

    override fun windowStateChanged(e: WindowEvent?) {
        when (e?.newState) {
            null -> Unit
            WINDOW_CLOSED -> window?.let {
                onFocusChange(window, false)
                unregister(it)
            }

            WINDOW_GAINED_FOCUS -> onFocusChange(window, true)
            WINDOW_ICONIFIED -> onFocusChange(window, false)
            WINDOW_DEICONIFIED -> onFocusChange(window, true)
            WINDOW_ACTIVATED -> onFocusChange(window, true)
            WINDOW_DEACTIVATED -> onFocusChange(window, false)
        }
    }
}
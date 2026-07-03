package org.mjdev.desktop.helpers.mouseevents

import java.awt.AWTEvent
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.MouseEvent

/**
 * Process-global mouse-press listener. Like [org.mjdev.desktop.helpers.keyevents.GlobalKeyListener]
 * but for clicks: it catches a press on ANY AWT window of this app and reports it in absolute
 * screen coordinates, so [org.mjdev.desktop.state.DesktopState] can decide whether the click
 * landed outside the open surfaces (and dismiss them).
 */
class GlobalClickListener(
    private val onPress: (screenX: Int, screenY: Int, button: Int) -> Unit,
) : AWTEventListener {
    init {
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK)
    }

    fun dispose() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this)
    }

    override fun eventDispatched(event: AWTEvent?) {
        val mouse = event as? MouseEvent ?: return
        if (mouse.id != MouseEvent.MOUSE_PRESSED) return
        val onScreen = mouse.locationOnScreen
        onPress(onScreen.x, onScreen.y, mouse.button)
    }
}

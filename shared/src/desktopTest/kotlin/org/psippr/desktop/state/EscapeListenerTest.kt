package org.psippr.desktop.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression: Escape left the desktop menu open, it only closed the registered surfaces. */
class EscapeListenerTest {
    private val state = DesktopState(CoroutineScope(Dispatchers.Unconfined))

    @Test
    fun escapeTellsTheListener() {
        var escapes = 0
        state.addEscapeListener { escapes++ }
        state.onEscape()
        assertEquals(1, escapes)
    }

    @Test
    fun removedListenerHearsNothing() {
        var escapes = 0
        val listener = {
            escapes++
            Unit
        }
        state.addEscapeListener(listener)
        state.removeEscapeListener(listener)
        state.onEscape()
        assertEquals(0, escapes)
    }
}

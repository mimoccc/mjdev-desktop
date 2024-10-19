/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.keyevents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import java.awt.AWTEvent
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.KeyEvent.VK_TAB
import java.awt.event.KeyEvent as AwtKeyEvent

class GlobalKeyListener(
    val onEvent: (event: AwtKeyEvent) -> Unit
) : AWTEventListener {
    init {
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK)
    }

    fun dispose() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this)
    }

    override fun eventDispatched(event: AWTEvent?) {
        (event as? AwtKeyEvent)?.also { kev ->
            val notConsumed = !kev.isConsumed
            val isAlt = kev.isAltDown
            val keyCode = kev.keyCode
            val isAltTab = (isAlt && keyCode == VK_TAB)
            if (isAltTab) return
            if (notConsumed) onEvent(kev)
        }
    }

    companion object {
        @Composable
        fun globalKeyEventHandler(
            isEnabled: () -> Boolean = { true },
            block: KeyEventHandler.() -> Unit,
        ) {
            DisposableEffect(Unit) {
                val handler = KeyEventHandler(isEnabled, block)
                val globalHandler = GlobalKeyListener { event ->
                    handler.onEvent(event)
                }
                onDispose {
                    globalHandler.dispose()
                }
            }
        }
    }
}
/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.input.key.*
import eu.mjdev.desktop.extensions.Custom.isPrintable
import java.awt.AWTEvent
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.KeyEvent as AwtKeyEvent

@Suppress("CanBeParameter", "unused", "MemberVisibilityCanBePrivate")
class KeyEventHandler(
    private val event: AwtKeyEvent,
    private val block: KeyEventHandler.() -> Unit
) {
    private val listeners = mutableListOf<KeyEventListener>()

    fun addListener(key: Key?, block: (Char) -> Boolean) {
        listeners.add(KeyEventListener(key, block))
    }

    init {
        block()
        val code = event.keyCode
        val char = parseCharacter(event)
        val isKeyUp = event.id == AwtKeyEvent.KEY_RELEASED
        when {
            isKeyUp && char.isPrintable -> {
                var consumed = false
                listeners.filter { l ->
                    l.key == null
                }.forEach { l ->
                    consumed = consumed || l.block(char)
                }
                if (consumed) event.consume()
            }

            isKeyUp -> {
                var consumed = false
                listeners.filter { l ->
                    l.key?.keyCode?.toInt() == code
                }.forEach { l ->
                    consumed = consumed || l.block(char)
                }
                if (consumed) event.consume()
            }

            else -> Unit
        }
    }

    private fun parseCharacter(event: AwtKeyEvent): Char = Char(event.keyCode).let { c ->
        if (event.isShiftDown) c.uppercaseChar() else c.lowercaseChar()
    }

    fun onChar(block: (Char) -> Boolean) = addListener(null, block)
    fun onEscape(block: (Char) -> Boolean) = addListener(Key.Escape, block)
    fun onDelete(block: (Char) -> Boolean) = addListener(Key.Delete, block)
    fun onEnter(block: (Char) -> Boolean) = addListener(Key.Enter, block)
    fun onBack(block: (Char) -> Boolean) = addListener(Key.Back, block)
    fun onBackSpace(block: (Char) -> Boolean) = addListener(Key.Backspace, block)

    class KeyEventListener(val key: Key?, val block: (Char) -> Boolean)

    class GlobalKeyListener(
        val enabled: Boolean = true,
        val onKey: (event: AwtKeyEvent) -> Unit
    ) : AWTEventListener {
        init {
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK)
        }

        fun dispose() {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this)
        }

        override fun eventDispatched(event: AWTEvent?) {
            (event as? AwtKeyEvent)?.also { kev ->
                if (enabled && !event.isConsumed) onKey(kev)
            }
        }
    }

    companion object {
        fun keyEventHandler(
            event: AwtKeyEvent,
            block: KeyEventHandler.() -> Unit
        ) = KeyEventHandler(event, block)

        @Composable
        fun globalKeyEventHandler(
            enabled: Boolean = true,
            block: KeyEventHandler.() -> Unit
        ) {
            val globalHandler = GlobalKeyListener(enabled) { event ->
                keyEventHandler(event, block)
            }
            DisposableEffect(Unit) {
                onDispose {
                    globalHandler.dispose()
                }
            }
        }
    }
}

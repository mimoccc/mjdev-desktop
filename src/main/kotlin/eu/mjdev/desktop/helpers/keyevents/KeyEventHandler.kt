/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.keyevents

import androidx.compose.ui.input.key.Key
import eu.mjdev.desktop.extensions.Custom.isPrintable
import eu.mjdev.desktop.log.Log
import java.awt.event.KeyEvent.VK_WINDOWS
import java.awt.event.KeyEvent as AwtKeyEvent

@Suppress("CanBeParameter", "unused", "MemberVisibilityCanBePrivate")
class KeyEventHandler(
    private val isEnabled: () -> Boolean = { true },
    private val block: KeyEventHandler.() -> Unit
) {
    private val listeners = mutableListOf<KeyEventListener>()

    fun addListener(key: Key?, block: (Char) -> Boolean) {
        listeners.add(KeyEventListenerCompose(key, block))
    }

    fun addListener(key: Int, block: (Int) -> Boolean) {
        listeners.add(KeyEventListenerAwt(key, block))
    }

    init {
        block()
    }

    fun onEvent(
        event: AwtKeyEvent
    ) {
        if (!isEnabled()) return
        val code = event.keyCode
        val char = parseCharacter(event)
        val isKeyUp = event.id == AwtKeyEvent.KEY_RELEASED
        Log.i("Got key: $code")
        when {
            isKeyUp && char.isPrintable -> {
                var consumed = false
                listeners.filterIsInstance<KeyEventListenerCompose>().filter { l ->
                    l.key == null
                }.forEach { l ->
                    consumed = consumed || l.block(char)
                }
                if (consumed) event.consume()
            }

            isKeyUp -> {
                var consumed = false
                listeners.filter { l ->
                    when (l) {
                        is KeyEventListenerCompose -> l.key?.keyCode?.toInt() == code
                        is KeyEventListenerAwt -> l.key == code
                        else -> false
                    }
                }.forEach { l ->
                    consumed = consumed || when (l) {
                        is KeyEventListenerCompose -> l.block(char)
                        is KeyEventListenerAwt -> l.block(code)
                        else -> false
                    }
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
    fun onMenuKey(block: (Int) -> Boolean) = addListener(VK_WINDOWS, block)
}

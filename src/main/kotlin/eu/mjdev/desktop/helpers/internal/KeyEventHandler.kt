/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.internal

import androidx.compose.ui.input.key.*

@Suppress("CanBeParameter", "unused", "MemberVisibilityCanBePrivate")
class KeyEventHandler(
    private val event: KeyEvent,
    private val block: KeyEventHandler.() -> Unit
) {
    private val listeners = mutableListOf<KeyEventListener>()

    fun addListener(key: Key?, block: (String) -> Unit) {
        listeners.add(KeyEventListener(key, block))
    }

    init {
        block()
        if (event.type == KeyEventType.KeyDown) {
            val char = Char(event.key.keyCode.toInt())
            when {
                char.isPrintable -> listeners.filter { l -> l.key == null }.forEach { l -> l.block(parseCharacter(event)) }
                else -> listeners.filter { l -> l.key == event.key }.forEach { l -> l.block(parseCharacter(event)) }
            }
        }
    }

    private val Char.isPrintable : Boolean get() {
        val block = Character.UnicodeBlock.of(this)
        return (!Character.isISOControl(this)) &&
                this != java.awt.event.KeyEvent.CHAR_UNDEFINED &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS
    }

    private fun parseCharacter(event: KeyEvent) = Char(event.key.keyCode.toInt()).let { c ->
        if (event.isShiftPressed) c.uppercase() else c.lowercase()
    }

    fun onChar(block: (String) -> Unit) = addListener(null, block)
    fun onEscape(block: (String) -> Unit) = addListener(Key.Escape, block)
    fun onDelete(block: (String) -> Unit) = addListener(Key.Delete, block)
    fun onEnter(block: (String) -> Unit) = addListener(Key.Enter, block)
    fun onBack(block: (String) -> Unit) = addListener(Key.Back, block)
    fun onBackSpace(block: (String) -> Unit) = addListener(Key.Back, block)

    class KeyEventListener(val key: Key?, val block: (String) -> Unit)

    companion object {
        fun keyEventHandler(
            event: KeyEvent,
            block: KeyEventHandler.() -> Unit
        ) = KeyEventHandler(event, block)
    }
}
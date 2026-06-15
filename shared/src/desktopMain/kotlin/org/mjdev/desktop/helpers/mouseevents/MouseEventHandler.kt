/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.helpers.mouseevents

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import java.awt.Point

@Suppress("CanBeParameter", "unused", "MemberVisibilityCanBePrivate")
class MouseEventHandler(
    private val isEnabled: () -> Boolean = { true },
    private val block: MouseEventHandler.() -> Unit,
) {
    private val listeners = mutableListOf<MouseEventListener>()

    init {
        block()
    }

    fun addListener(
        type: MouseEventType,
        range: MouseRange,
        block: (offset: DpOffset) -> Unit,
    ) = listeners.add(MouseEventListener(type, range, block))

    fun onEvent(point: Point) {
        if (!isEnabled()) return
        val offset = DpOffset(point.x.dp, point.y.dp)
        // Edge-triggered: fire only when the pointer crosses a range boundary, never on every
        // in/out sample. This stops the high-frequency pointer feed from flooding the coroutine
        // scope (which froze the bars) and from flickering the windows by re-firing show/hide.
        listeners.forEach { listener ->
            val inRange = listener.isInRange(offset)
            val wasInRange = listener.lastInRange
            listener.lastInRange = inRange
            when (listener.type) {
                MouseEventType.ENTER -> if (inRange && !wasInRange) listener.block(offset)
                MouseEventType.LEAVE -> if (!inRange && wasInRange) listener.block(offset)
            }
        }
    }

    fun onPointerEnter(
        range: MouseRange,
        block: (offset: DpOffset) -> Unit,
    ) = addListener(MouseEventType.ENTER, range, block)

    fun onPointerLeave(
        range: MouseRange,
        block: (offset: DpOffset) -> Unit,
    ) = addListener(MouseEventType.LEAVE, range, block)
}

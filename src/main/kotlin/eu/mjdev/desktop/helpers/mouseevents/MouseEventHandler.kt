/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.mouseevents

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import java.awt.Point

@Suppress("CanBeParameter", "unused", "MemberVisibilityCanBePrivate")
class MouseEventHandler(
    private val isEnabled: () -> Boolean = { true },
    private val block: MouseEventHandler.() -> Unit
) {
    private val listeners = mutableListOf<MouseEventListener>()

    init {
        block()
    }

    fun addListener(
        type: MouseEventType,
        range: MouseRange,
        block: (offset: DpOffset) -> Unit
    ) = listeners.add(MouseEventListener(type, range, block))

    fun onEvent(point: Point) {
        if (!isEnabled()) return
        val offset = DpOffset(point.x.dp, point.y.dp)
        listeners.filter { ev ->
            ev.type == MouseEventType.ENTER
        }.filter { ev ->
            ev.isInRange(offset)
        }.forEach { l ->
            l.block(offset)
        }
        listeners.filter { ev ->
            ev.type == MouseEventType.LEAVE
        }.filter { ev ->
            !ev.isInRange(offset)
        }.forEach { l ->
            l.block(offset)
        }
    }

    fun onPointerEnter(
        range: MouseRange,
        block: (offset: DpOffset) -> Unit
    ) = addListener(MouseEventType.ENTER, range, block)

    fun onPointerLeave(
        range: MouseRange,
        block: (offset: DpOffset) -> Unit
    ) = addListener(MouseEventType.LEAVE, range, block)

}

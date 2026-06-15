/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.helpers.mouseevents

import androidx.compose.ui.unit.DpOffset

@Suppress("MemberVisibilityCanBePrivate")
class MouseEventListener(
    val type: MouseEventType,
    val range: MouseRange,
    val block: (offset: DpOffset) -> Unit,
) {
    /**
     * Last known in-range state, used for edge-triggering. The pointer feed is high frequency
     * (compositor streams every motion sample), so firing on every in/out sample floods the
     * coroutine scope and flickers the UI. We instead fire only on the boundary crossing.
     */
    var lastInRange: Boolean = false

    fun isInRange(offset: DpOffset): Boolean {
        val inX = offset.x >= range.x && offset.x <= (range.x + range.width)
        val inY = offset.y >= range.y && offset.y <= (range.y + range.height)
        return inX && inY
    }
}

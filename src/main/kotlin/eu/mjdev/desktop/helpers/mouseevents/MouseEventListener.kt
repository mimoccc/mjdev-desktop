/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.mouseevents

import androidx.compose.ui.unit.DpOffset

@Suppress("MemberVisibilityCanBePrivate")
class MouseEventListener(
    val type: MouseEventType,
    val range: MouseRange,
    val block: (offset: DpOffset) -> Unit
) {
    fun isInRange(
        offset: DpOffset
    ): Boolean {
        val inX = offset.x >= range.x && offset.x <= (range.x + range.width)
        val inY = offset.y >= range.y && offset.y <= (range.y + range.height)
        return inX && inY
    }
}
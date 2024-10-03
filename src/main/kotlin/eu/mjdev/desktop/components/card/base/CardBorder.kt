/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.card.base

import androidx.compose.runtime.Immutable
import eu.mjdev.desktop.components.surface.base.Border

@Immutable
class CardBorder
internal constructor(
    internal val border: Border,
    internal val focusedBorder: Border,
    internal val pressedBorder: Border
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CardBorder
        if (border != other.border) return false
        if (focusedBorder != other.focusedBorder) return false
        if (pressedBorder != other.pressedBorder) return false
        return true
    }

    override fun hashCode(): Int {
        var result = border.hashCode()
        result = 31 * result + focusedBorder.hashCode()
        result = 31 * result + pressedBorder.hashCode()
        return result
    }

    override fun toString(): String {
        return "CardBorder(border=$border, focusedBorder=$focusedBorder, " +
            "pressedBorder=$pressedBorder)"
    }
}
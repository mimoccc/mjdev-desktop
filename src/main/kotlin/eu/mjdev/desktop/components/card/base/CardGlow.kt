/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.card.base

import androidx.compose.runtime.Immutable
import eu.mjdev.desktop.components.surface.base.Glow

@Immutable
class CardGlow(
    val glow: Glow,
    val focusedGlow: Glow,
    val pressedGlow: Glow
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CardGlow
        if (glow != other.glow) return false
        if (focusedGlow != other.focusedGlow) return false
        if (pressedGlow != other.pressedGlow) return false
        return true
    }

    override fun hashCode(): Int {
        var result = glow.hashCode()
        result = 31 * result + focusedGlow.hashCode()
        result = 31 * result + pressedGlow.hashCode()
        return result
    }

    override fun toString(): String {
        return "CardGlow(glow=$glow, focusedGlow=$focusedGlow, pressedGlow=$pressedGlow)"
    }
}
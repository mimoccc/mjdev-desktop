/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.card.base

import androidx.compose.runtime.Immutable

@Suppress("unused")
@Immutable
class CardScale
internal constructor(
    internal val scale: Float,
    internal val focusedScale: Float,
    internal val pressedScale: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CardScale
        if (scale != other.scale) return false
        if (focusedScale != other.focusedScale) return false
        if (pressedScale != other.pressedScale) return false
        return true
    }

    override fun hashCode(): Int {
        var result = scale.hashCode()
        result = 31 * result + focusedScale.hashCode()
        result = 31 * result + pressedScale.hashCode()
        return result
    }

    override fun toString(): String {
        return "CardScale(scale=$scale, focusedScale=$focusedScale, pressedScale=$pressedScale)"
    }

    companion object {
        val None = CardScale(scale = 1f, focusedScale = 1f, pressedScale = 1f)
    }
}
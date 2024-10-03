/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.card.base

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape

@Immutable
class CardShape
internal constructor(
    internal val shape: Shape,
    internal val focusedShape: Shape,
    internal val pressedShape: Shape
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CardShape
        if (shape != other.shape) return false
        if (focusedShape != other.focusedShape) return false
        if (pressedShape != other.pressedShape) return false
        return true
    }

    override fun hashCode(): Int {
        var result = shape.hashCode()
        result = 31 * result + focusedShape.hashCode()
        result = 31 * result + pressedShape.hashCode()
        return result
    }

    override fun toString(): String {
        return "CardShape(shape=$shape, focusedShape=$focusedShape, pressedShape=$pressedShape)"
    }
}


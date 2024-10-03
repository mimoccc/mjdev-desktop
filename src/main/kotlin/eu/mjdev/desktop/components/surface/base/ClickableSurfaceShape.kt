/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.surface.base

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape

@Immutable
class ClickableSurfaceShape
internal constructor(
    internal val shape: Shape,
    internal val focusedShape: Shape,
    internal val pressedShape: Shape,
    internal val disabledShape: Shape,
    internal val focusedDisabledShape: Shape
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ClickableSurfaceShape
        if (shape != other.shape) return false
        if (focusedShape != other.focusedShape) return false
        if (pressedShape != other.pressedShape) return false
        if (disabledShape != other.disabledShape) return false
        if (focusedDisabledShape != other.focusedDisabledShape) return false
        return true
    }

    override fun hashCode(): Int {
        var result = shape.hashCode()
        result = 31 * result + focusedShape.hashCode()
        result = 31 * result + pressedShape.hashCode()
        result = 31 * result + disabledShape.hashCode()
        result = 31 * result + focusedDisabledShape.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClickableSurfaceShape(shape=$shape, focusedShape=$focusedShape, " +
                "pressedShape=$pressedShape, disabledShape=$disabledShape, " +
                "focusedDisabledShape=$focusedDisabledShape)"
    }
}

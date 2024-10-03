/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.surface.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
class Border(
    val border: BorderStroke,
    val inset: Dp = 0.dp,
    val shape: Shape = ShapeTokens.BorderDefaultShape
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Border
        if (border != other.border) return false
        if (inset != other.inset) return false
        if (shape != other.shape) return false
        return true
    }

    override fun hashCode(): Int {
        var result = border.hashCode()
        result = 31 * result + inset.hashCode()
        result = 31 * result + shape.hashCode()
        return result
    }

    override fun toString(): String {
        return "Border(border=$border, inset=$inset, shape=$shape)"
    }

    fun copy(border: BorderStroke? = null, inset: Dp? = null, shape: Shape? = null): Border =
        Border(
            border = border ?: this.border,
            inset = inset ?: this.inset,
            shape = shape ?: this.shape
        )

    companion object {
        val None =
            Border(
                border = BorderStroke(width = 0.dp, color = Color.Transparent),
                inset = 0.dp,
                shape = RectangleShape
            )
    }
}
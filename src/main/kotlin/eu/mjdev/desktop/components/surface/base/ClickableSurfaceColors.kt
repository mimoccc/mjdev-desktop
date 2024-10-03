/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.surface.base

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
class ClickableSurfaceColors
    (
    val containerColor: Color,
    val contentColor: Color,
    val focusedContainerColor: Color,
    val focusedContentColor: Color,
    val pressedContainerColor: Color,
    val pressedContentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ClickableSurfaceColors
        if (containerColor != other.containerColor) return false
        if (contentColor != other.contentColor) return false
        if (focusedContainerColor != other.focusedContainerColor) return false
        if (focusedContentColor != other.focusedContentColor) return false
        if (pressedContainerColor != other.pressedContainerColor) return false
        if (pressedContentColor != other.pressedContentColor) return false
        if (disabledContainerColor != other.disabledContainerColor) return false
        if (disabledContentColor != other.disabledContentColor) return false
        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + contentColor.hashCode()
        result = 31 * result + focusedContainerColor.hashCode()
        result = 31 * result + focusedContentColor.hashCode()
        result = 31 * result + pressedContainerColor.hashCode()
        result = 31 * result + pressedContentColor.hashCode()
        result = 31 * result + disabledContainerColor.hashCode()
        result = 31 * result + disabledContentColor.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClickableSurfaceColors(" +
                "containerColor=$containerColor, " +
                "contentColor=$contentColor, " +
                "focusedContainerColor=$focusedContainerColor, " +
                "focusedContentColor=$focusedContentColor, " +
                "pressedContainerColor=$pressedContainerColor, " +
                "pressedContentColor=$pressedContentColor, " +
                "disabledContainerColor=$disabledContainerColor, " +
                "disabledContentColor=$disabledContentColor)"
    }
}

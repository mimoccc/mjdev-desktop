/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.card.base

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
class CardColors
internal constructor(
    internal val containerColor: Color,
    internal val contentColor: Color,
    internal val focusedContainerColor: Color,
    internal val focusedContentColor: Color,
    internal val pressedContainerColor: Color,
    internal val pressedContentColor: Color
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CardColors
        if (containerColor != other.containerColor) return false
        if (contentColor != other.contentColor) return false
        if (focusedContainerColor != other.focusedContainerColor) return false
        if (focusedContentColor != other.focusedContentColor) return false
        if (pressedContainerColor != other.pressedContainerColor) return false
        if (pressedContentColor != other.pressedContentColor) return false
        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + contentColor.hashCode()
        result = 31 * result + focusedContainerColor.hashCode()
        result = 31 * result + focusedContentColor.hashCode()
        result = 31 * result + pressedContainerColor.hashCode()
        result = 31 * result + pressedContentColor.hashCode()
        return result
    }

    override fun toString(): String {
        return "CardColors(" +
                "containerColor=$containerColor, " +
                "contentColor=$contentColor, " +
                "focusedContainerColor=$focusedContainerColor, " +
                "focusedContentColor=$focusedContentColor, " +
                "pressedContainerColor=$pressedContainerColor, " +
                "pressedContentColor=$pressedContentColor)"
    }
}
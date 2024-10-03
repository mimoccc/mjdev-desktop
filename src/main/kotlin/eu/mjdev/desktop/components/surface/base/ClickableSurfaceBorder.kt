/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.surface.base

import androidx.compose.runtime.Immutable

@Immutable
class ClickableSurfaceBorder
internal constructor(
    internal val border: Border,
    internal val focusedBorder: Border,
    internal val pressedBorder: Border,
    internal val disabledBorder: Border,
    internal val focusedDisabledBorder: Border
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ClickableSurfaceBorder
        if (border != other.border) return false
        if (focusedBorder != other.focusedBorder) return false
        if (pressedBorder != other.pressedBorder) return false
        if (disabledBorder != other.disabledBorder) return false
        if (focusedDisabledBorder != other.focusedDisabledBorder) return false
        return true
    }

    override fun hashCode(): Int {
        var result = border.hashCode()
        result = 31 * result + focusedBorder.hashCode()
        result = 31 * result + pressedBorder.hashCode()
        result = 31 * result + disabledBorder.hashCode()
        result = 31 * result + focusedDisabledBorder.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClickableSurfaceBorder(border=$border, " +
                "focusedBorder=$focusedBorder, " +
                "pressedBorder=$pressedBorder, " +
                "disabledBorder=$disabledBorder, " +
                "focusedDisabledBorder=$focusedDisabledBorder)"
    }
}

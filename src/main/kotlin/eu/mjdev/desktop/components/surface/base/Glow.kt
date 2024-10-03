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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
class Glow(val elevationColor: Color, val elevation: Dp) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Glow
        if (elevationColor != other.elevationColor) return false
        if (elevation != other.elevation) return false
        return true
    }

    override fun hashCode(): Int {
        var result = elevationColor.hashCode()
        result = 31 * result + elevation.hashCode()
        return result
    }

    override fun toString(): String {
        return "Glow(elevationColor=$elevationColor, elevation=$elevation)"
    }

    fun copy(glowColor: Color? = null, glowElevation: Dp? = null): Glow =
        Glow(
            elevationColor = glowColor ?: this.elevationColor,
            elevation = glowElevation ?: this.elevation
        )

    companion object {
        val None = Glow(elevationColor = Color.Transparent, elevation = 0.dp)
    }
}
/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.surface.base

import androidx.compose.runtime.Immutable

@Suppress("unused")
@Immutable
class ClickableSurfaceScale
internal constructor(
    internal val scale: Float,
    internal val focusedScale: Float,
    internal val pressedScale: Float,
    internal val disabledScale: Float,
    internal val focusedDisabledScale: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ClickableSurfaceScale
        if (scale != other.scale) return false
        if (focusedScale != other.focusedScale) return false
        if (pressedScale != other.pressedScale) return false
        if (disabledScale != other.disabledScale) return false
        if (focusedDisabledScale != other.focusedDisabledScale) return false
        return true
    }

    override fun hashCode(): Int {
        var result = scale.hashCode()
        result = 31 * result + focusedScale.hashCode()
        result = 31 * result + pressedScale.hashCode()
        result = 31 * result + disabledScale.hashCode()
        result = 31 * result + focusedDisabledScale.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClickableSurfaceScale(scale=$scale, focusedScale=$focusedScale," +
                "pressedScale=$pressedScale, disabledScale=$disabledScale, " +
                "focusedDisabledScale=$focusedDisabledScale)"
    }

    companion object {
        val None =
            ClickableSurfaceScale(
                scale = 1f,
                focusedScale = 1f,
                pressedScale = 1f,
                disabledScale = 1f,
                focusedDisabledScale = 1f
            )
    }
}

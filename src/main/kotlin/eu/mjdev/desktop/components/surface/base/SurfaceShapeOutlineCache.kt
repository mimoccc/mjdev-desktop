/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.surface.base

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

internal class SurfaceShapeOutlineCache(
    private var shape: Shape,
    private var size: Size,
    private var layoutDirection: LayoutDirection,
    private var density: Density
) {
    private var outline: Outline? = null

    fun updatedOutline(
        shape: Shape,
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (outline == null || hasUpdates(shape, size, layoutDirection, density)) {
            syncUpdates(shape, size, layoutDirection, density)
            createNewOutline()
        }
        return outline!!
    }

    private fun createNewOutline() {
        outline = shape.createOutline(size = size, layoutDirection = layoutDirection, density = density)
    }

    private fun syncUpdates(
        shape: Shape,
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) {
        this.shape = shape
        this.size = size
        this.layoutDirection = layoutDirection
        this.density = density
    }

    private fun hasUpdates(
        shape: Shape,
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Boolean {
        if (shape != this.shape) return true
        if (size != this.size) return true
        if (layoutDirection != this.layoutDirection) return true
        if (density != this.density) return true
        return false
    }
}

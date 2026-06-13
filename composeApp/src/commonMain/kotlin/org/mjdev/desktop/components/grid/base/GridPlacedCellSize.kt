/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.grid.base

import androidx.compose.ui.unit.Dp

@Suppress("unused")
sealed class GridPlacedCellSize {
    data class Fixed(
        val size: Dp,
    ) : GridPlacedCellSize() {
        init {
            check(size.value > 0) { "size have to be > 0" }
        }
    }

    data class Weight(
        val size: Float = 1f,
    ) : GridPlacedCellSize() {
        init {
            check(size > 0) { "size have to be > 0" }
        }
    }

    companion object {
        private fun <T> mutableListOfElement(
            size: Int,
            fillElement: T,
        ): MutableList<T> = (0 until size).map { fillElement }.toMutableList()

        fun fixed(
            count: Int,
            size: Dp,
        ): MutableList<GridPlacedCellSize> = mutableListOfElement(count, Fixed(size = size))

        fun fixed(sizes: Array<Dp>): MutableList<GridPlacedCellSize> = sizes.map { Fixed(size = it) }.toMutableList()

        fun weight(
            count: Int,
            size: Float = 1f,
        ): MutableList<GridPlacedCellSize> = mutableListOfElement(count, Weight(size = size))

        fun weight(vararg sizes: Float): MutableList<GridPlacedCellSize> = sizes.map { Weight(size = it) }.toMutableList()
    }
}

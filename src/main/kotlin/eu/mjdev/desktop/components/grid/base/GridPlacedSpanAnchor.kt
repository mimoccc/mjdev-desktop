/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.grid.base

data class GridPlacedSpanAnchor(val horizontal: Horizontal, val vertical: Vertical) {
    enum class Horizontal {
        START,
        END
    }

    enum class Vertical {
        TOP,
        BOTTOM
    }

    companion object {
        fun GridPlacedSpanAnchor.leftBound(column: Int, span: Int): Int {
            return when (horizontal) {
                Horizontal.START -> column
                Horizontal.END -> column - span + 1
            }
        }

        fun GridPlacedSpanAnchor.rightBound(column: Int, span: Int): Int {
            return when (this.horizontal) {
                Horizontal.START -> column + span - 1
                Horizontal.END -> column
            }
        }

        fun GridPlacedSpanAnchor.topBound(row: Int, span: Int): Int {
            return when (this.vertical) {
                Vertical.TOP -> row
                Vertical.BOTTOM -> row - span + 1
            }
        }

        fun GridPlacedSpanAnchor.bottomBound(row: Int, span: Int): Int {
            return when (this.vertical) {
                Vertical.TOP -> row + span - 1
                Vertical.BOTTOM -> row
            }
        }
    }
}

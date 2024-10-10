/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.grid.base

data class GridPlacedPlacementPolicy(
    val mainAxis: MainAxis = MainAxis.HORIZONTAL,
    val horizontalDirection: HorizontalDirection = HorizontalDirection.START_END,
    val verticalDirection: VerticalDirection = VerticalDirection.TOP_BOTTOM
) {
    internal val anchor: GridPlacedSpanAnchor = run {
        val horizontalDirection = when (this.horizontalDirection) {
            HorizontalDirection.START_END -> GridPlacedSpanAnchor.Horizontal.START
            HorizontalDirection.END_START -> GridPlacedSpanAnchor.Horizontal.END
        }
        val verticalDirection = when (this.verticalDirection) {
            VerticalDirection.TOP_BOTTOM -> GridPlacedSpanAnchor.Vertical.TOP
            VerticalDirection.BOTTOM_TOP -> GridPlacedSpanAnchor.Vertical.BOTTOM
        }
        GridPlacedSpanAnchor(horizontalDirection, verticalDirection)
    }

    enum class MainAxis {
        HORIZONTAL,
        VERTICAL
    }

    enum class HorizontalDirection {
        START_END,
        END_START
    }

    enum class VerticalDirection {
        TOP_BOTTOM,
        BOTTOM_TOP
    }

    companion object {
        val DEFAULT: GridPlacedPlacementPolicy = GridPlacedPlacementPolicy()
    }
}

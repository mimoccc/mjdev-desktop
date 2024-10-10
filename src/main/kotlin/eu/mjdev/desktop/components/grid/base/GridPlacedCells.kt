/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.grid.base

import eu.mjdev.desktop.components.grid.base.GridPlacedSpanAnchor.Companion.bottomBound
import eu.mjdev.desktop.components.grid.base.GridPlacedSpanAnchor.Companion.leftBound
import eu.mjdev.desktop.components.grid.base.GridPlacedSpanAnchor.Companion.rightBound
import eu.mjdev.desktop.components.grid.base.GridPlacedSpanAnchor.Companion.topBound
import eu.mjdev.desktop.components.grid.base.Measure.calculateTotalSize

@Suppress("unused")
data class GridPlacedCells(
    val rowSizes: List<GridPlacedCellSize>,
    val columnSizes: List<GridPlacedCellSize>
) {
    constructor(
        rowSizes: Iterable<GridPlacedCellSize>, columnSizes: Iterable<GridPlacedCellSize>
    ) : this(rowSizes = rowSizes.toList(), columnSizes = columnSizes.toList())

    constructor(rowCount: Int, columnCount: Int) : this(
        rowSizes = GridPlacedCellSize.weight(rowCount),
        columnSizes = GridPlacedCellSize.weight(columnCount)
    )

    val rowCount: Int = rowSizes.size
    val columnCount: Int = columnSizes.size

    val rowsTotalSize: TotalSize = rowSizes.calculateTotalSize()
    val columnsTotalSize: TotalSize = columnSizes.calculateTotalSize()

    class Builder(rowCount: Int, columnCount: Int) {
        private val rowSizes: MutableList<GridPlacedCellSize> = GridPlacedCellSize.weight(rowCount)
        private val columnSizes: MutableList<GridPlacedCellSize> = GridPlacedCellSize.weight(columnCount)

        fun rowSize(index: Int, size: GridPlacedCellSize): Builder = apply {
            rowSizes[index] = size
        }

        fun rowsSize(size: GridPlacedCellSize): Builder = apply {
            rowSizes.fill(size)
        }

        fun columnSize(index: Int, size: GridPlacedCellSize): Builder = apply {
            columnSizes[index] = size
        }

        fun columnsSize(size: GridPlacedCellSize): Builder = apply {
            columnSizes.fill(size)
        }

        fun build(): GridPlacedCells =
            GridPlacedCells(rowSizes = rowSizes, columnSizes = columnSizes)
    }

    companion object {
        fun GridPlacedCells.firstRow(placementPolicy: GridPlacedPlacementPolicy): Int {
            return when (placementPolicy.verticalDirection) {
                GridPlacedPlacementPolicy.VerticalDirection.TOP_BOTTOM -> 0
                GridPlacedPlacementPolicy.VerticalDirection.BOTTOM_TOP -> rowCount - 1
            }
        }

        fun GridPlacedCells.firstColumn(placementPolicy: GridPlacedPlacementPolicy): Int {
            return when (placementPolicy.horizontalDirection) {
                GridPlacedPlacementPolicy.HorizontalDirection.START_END -> 0
                GridPlacedPlacementPolicy.HorizontalDirection.END_START -> columnCount - 1
            }
        }

        fun GridPlacedCells.isRowOutsideOfGrid(
            row: Int,
            rowSpan: Int,
            anchor: GridPlacedSpanAnchor
        ): Boolean {
            val top = anchor.topBound(row, rowSpan)
            val bottom = anchor.bottomBound(row, rowSpan)
            return top < 0 || bottom >= rowCount
        }

        fun GridPlacedCells.isColumnOutsideOfGrid(
            column: Int,
            columnSpan: Int,
            anchor: GridPlacedSpanAnchor
        ): Boolean {
            val left = anchor.leftBound(column, columnSpan)
            val right = anchor.rightBound(column, columnSpan)
            return left < 0 || right >= columnCount
        }
    }
}

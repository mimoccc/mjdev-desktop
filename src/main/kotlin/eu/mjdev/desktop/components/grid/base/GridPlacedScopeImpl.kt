/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.grid.base

import androidx.compose.runtime.Composable
import eu.mjdev.desktop.components.grid.base.GridPlacedCells.Companion.firstColumn
import eu.mjdev.desktop.components.grid.base.GridPlacedCells.Companion.firstRow
import eu.mjdev.desktop.components.grid.base.GridPlacedCells.Companion.isColumnOutsideOfGrid
import eu.mjdev.desktop.components.grid.base.GridPlacedCells.Companion.isRowOutsideOfGrid
import eu.mjdev.desktop.components.grid.base.GridPlacedSpanAnchor.Companion.bottomBound
import eu.mjdev.desktop.components.grid.base.GridPlacedSpanAnchor.Companion.leftBound
import eu.mjdev.desktop.components.grid.base.GridPlacedSpanAnchor.Companion.rightBound
import eu.mjdev.desktop.components.grid.base.GridPlacedSpanAnchor.Companion.topBound

class GridPlacedScopeImpl(
    private val cells: GridPlacedCells,
    private val placementPolicy: GridPlacedPlacementPolicy
) : GridPlacedScope {
    val data: MutableList<GridPlacedContent> = mutableListOf()

    override fun item(
        row: Int,
        column: Int,
        rowSpan: Int,
        columnSpan: Int,
        itemContent: @Composable GridPlacedItemScope.() -> Unit
    ) {
        checkSpan(rowSpan = rowSpan, columnSpan = columnSpan)
        placeExplicitly(
            row = row,
            column = column,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            itemContent = itemContent
        )
    }

    override fun item(
        rowSpan: Int,
        columnSpan: Int,
        itemContent: @Composable GridPlacedItemScope.() -> Unit
    ) {
        checkSpan(rowSpan = rowSpan, columnSpan = columnSpan)
        placeImplicitly(
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            itemContent = itemContent
        )
    }

    private fun checkSpan(rowSpan: Int, columnSpan: Int) {
        check(rowSpan > 0) { "`rowSpan` must be > 0" }
        check(columnSpan > 0) { "`columnSpan` must be > 0" }
    }

    private fun placeImplicitly(
        rowSpan: Int,
        columnSpan: Int,
        itemContent: @Composable GridPlacedItemScope.() -> Unit
    ) {
        val anchor = placementPolicy.anchor
        val lastItem = data.lastOrNull()
        var row: Int
        var column: Int
        when (placementPolicy.mainAxis) {
            GridPlacedPlacementPolicy.MainAxis.HORIZONTAL -> {
                row = findCurrentRow(cells, placementPolicy, lastItem)
                column = findNextColumn(cells, placementPolicy, lastItem)
                if (cells.isColumnOutsideOfGrid(column, columnSpan, anchor)) {
                    column = cells.firstColumn(placementPolicy)
                    row = findNextRow(cells, placementPolicy, lastItem)
                }
            }

            GridPlacedPlacementPolicy.MainAxis.VERTICAL -> {
                column = findCurrentColumn(cells, placementPolicy, lastItem)
                row = findNextRow(cells, placementPolicy, lastItem)
                if (cells.isRowOutsideOfGrid(row, rowSpan, anchor)) {
                    row = cells.firstRow(placementPolicy)
                    column = findNextColumn(cells, placementPolicy, lastItem)
                }
            }
        }
        placeExplicitly(
            row = row,
            column = column,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            itemContent = itemContent
        )
    }

    private fun placeExplicitly(
        row: Int,
        column: Int,
        rowSpan: Int,
        columnSpan: Int,
        itemContent: @Composable GridPlacedItemScope.() -> Unit
    ) {
        val anchor = placementPolicy.anchor
        val rowOutside = cells.isRowOutsideOfGrid(row, rowSpan, anchor)
        val columnOutside = cells.isColumnOutsideOfGrid(column, columnSpan, anchor)
        if (rowOutside || columnOutside) {
            onSkipped(row, column, rowSpan, columnSpan)
        } else {
            onPlaced(
                left = anchor.leftBound(column, columnSpan),
                top = anchor.topBound(row, rowSpan),
                right = anchor.rightBound(column, columnSpan),
                bottom = anchor.bottomBound(row, rowSpan),
                itemContent = itemContent
            )
        }
    }

    private fun onSkipped(row: Int?, column: Int?, rowSpan: Int, columnSpan: Int) {
        println(
            """
                Skipped position: [${row}x$column], span size: [${rowSpan}x$columnSpan]
                Grid size: [${cells.rowCount}x${cells.columnCount}]
            """.trimIndent()
        )
    }

    private fun onPlaced(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        itemContent: @Composable GridPlacedItemScope.() -> Unit
    ) {
        data.add(
            GridPlacedContent(
                top = top,
                left = left,
                right = right,
                bottom = bottom,
                item = { itemContent() }
            )
        )
    }

    private fun findCurrentRow(
        cells: GridPlacedCells,
        placementPolicy: GridPlacedPlacementPolicy,
        lastItem: GridPlacedContent?
    ): Int {
        return if (lastItem != null) {
            when (placementPolicy.verticalDirection) {
                GridPlacedPlacementPolicy.VerticalDirection.TOP_BOTTOM -> lastItem.top
                GridPlacedPlacementPolicy.VerticalDirection.BOTTOM_TOP -> lastItem.bottom
            }
        } else {
            cells.firstRow(placementPolicy)
        }
    }

    private fun findNextRow(
        cells: GridPlacedCells,
        placementPolicy: GridPlacedPlacementPolicy,
        lastItem: GridPlacedContent?
    ): Int {
        val lastRow = findCurrentRow(cells, placementPolicy, lastItem)
        val lastRowSpan = lastItem?.rowSpan ?: 0
        return when (placementPolicy.verticalDirection) {
            GridPlacedPlacementPolicy.VerticalDirection.TOP_BOTTOM -> lastRow + lastRowSpan
            GridPlacedPlacementPolicy.VerticalDirection.BOTTOM_TOP -> lastRow - lastRowSpan
        }
    }

    private fun findCurrentColumn(
        cells: GridPlacedCells,
        placementPolicy: GridPlacedPlacementPolicy,
        lastItem: GridPlacedContent?
    ): Int {
        return if (lastItem != null) {
            when (placementPolicy.horizontalDirection) {
                GridPlacedPlacementPolicy.HorizontalDirection.START_END -> lastItem.left
                GridPlacedPlacementPolicy.HorizontalDirection.END_START -> lastItem.right
            }
        } else {
            cells.firstColumn(placementPolicy)
        }
    }

    private fun findNextColumn(
        cells: GridPlacedCells,
        placementPolicy: GridPlacedPlacementPolicy,
        lastItem: GridPlacedContent?
    ): Int {
        val lastColumn = findCurrentColumn(cells, placementPolicy, lastItem)
        val lastColumnSpan = lastItem?.columnSpan ?: 0
        return when (placementPolicy.horizontalDirection) {
            GridPlacedPlacementPolicy.HorizontalDirection.START_END -> lastColumn + lastColumnSpan
            GridPlacedPlacementPolicy.HorizontalDirection.END_START -> lastColumn - lastColumnSpan
        }
    }
}

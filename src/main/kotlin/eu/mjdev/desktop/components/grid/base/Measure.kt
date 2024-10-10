/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.grid.base

import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.roundToInt

object Measure {
    fun List<Measurable>.measure(
        content: List<GridPlacedContent>,
        cellPlaces: Array<Array<CellPlacedInfo>>,
        constraints: Constraints
    ): List<Placeable> = mapIndexed { index, measurable ->
        val contentMetaInfo = content[index]
        val maxWidth = (contentMetaInfo.left..contentMetaInfo.right).sumOf { column ->
            cellPlaces[contentMetaInfo.top][column].width
        }
        val maxHeight = (contentMetaInfo.top..contentMetaInfo.bottom).sumOf { row ->
            cellPlaces[row][contentMetaInfo.left].height
        }
        measurable.measure(
            constraints.copy(
                minWidth = min(constraints.minWidth, maxWidth),
                maxWidth = maxWidth,
                minHeight = min(constraints.minHeight, maxHeight),
                maxHeight = maxHeight
            )
        )
    }

    fun MeasureScope.calculateCellPlaces(
        cells: GridPlacedCells, width: Int, height: Int
    ): Array<Array<CellPlacedInfo>> {
        val columnWidths = calculateSizesForDimension(width, cells.columnSizes, cells.columnsTotalSize)
        val columnHeights = calculateSizesForDimension(height, cells.rowSizes, cells.rowsTotalSize)
        var y = 0f
        val cellPlaces = columnHeights.map { columnHeight ->
            var x = 0f
            val cellY = y
            y += columnHeight
            columnWidths.map { columnWidth ->
                val cellX = x
                x += columnWidth
                CellPlacedInfo(
                    x = cellX.roundToInt(),
                    y = cellY.roundToInt(),
                    width = columnWidth,
                    height = columnHeight
                )
            }
        }
        return cellPlaces.map { it.toTypedArray() }.toTypedArray()
    }

    private fun MeasureScope.calculateSizesForDimension(
        availableSize: Int, cellSizes: List<GridPlacedCellSize>, totalSize: TotalSize
    ): List<Int> {
        val availableWeight = availableSize - totalSize.fixed.toPx()
        var reminder = 0f
        return cellSizes.map { cellSize ->
            when (cellSize) {
                is GridPlacedCellSize.Fixed -> {
                    val floatSize = cellSize.size.toPx() + reminder
                    val size = floatSize.roundToInt()
                    reminder = floatSize - size
                    size
                }

                is GridPlacedCellSize.Weight -> {
                    val floatSize = availableWeight * cellSize.size / totalSize.weight + reminder
                    val size = floatSize.roundToInt()
                    reminder = floatSize - size
                    size
                }
            }
        }.toMutableList()
    }

    fun Iterable<GridPlacedCellSize>.calculateTotalSize(): TotalSize {
        var totalWeightSize = 0f
        var totalFixedSize = 0f.dp
        forEach {
            when (it) {
                is GridPlacedCellSize.Weight -> totalWeightSize += it.size
                is GridPlacedCellSize.Fixed -> totalFixedSize += it.size
            }
        }
        return TotalSize(weight = totalWeightSize, fixed = totalFixedSize)
    }
}

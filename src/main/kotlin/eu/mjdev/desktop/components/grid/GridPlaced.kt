package eu.mjdev.desktop.components.grid

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import eu.mjdev.desktop.components.grid.base.*
import eu.mjdev.desktop.components.grid.base.Measure.calculateCellPlaces
import eu.mjdev.desktop.components.grid.base.Measure.measure
import eu.mjdev.desktop.extensions.Compose.preview
import kotlin.math.min

@Composable
fun GridPlaced(
    modifier: Modifier = Modifier,
    cells: GridPlacedCells = GridPlacedCells(2, 2),
    placementPolicy: GridPlacedPlacementPolicy = GridPlacedPlacementPolicy.DEFAULT,
    content: GridPlacedScope.() -> Unit = {}
) {
    val scopeContent: GridPlacedScopeImpl = GridPlacedScopeImpl(cells, placementPolicy).apply(content)
    val displayContent: List<GridPlacedContent> = scopeContent.data.toList()
    Layout(
        modifier = modifier,
        content = {
            displayContent.forEach { cnt -> cnt.item(GridPlacedItemScopeImpl) }
        }
    ) { measurables, constraints ->
        check(constraints.maxWidth != Constraints.Infinity) { "Infinity width not allowed in GridPad" }
        check(constraints.maxHeight != Constraints.Infinity) { "Infinity height not allowed in GridPad" }
        val cellPlaces = calculateCellPlaces(cells, width = constraints.maxWidth, height = constraints.maxHeight)
        val placeables = measurables.measure(displayContent, cellPlaces, constraints)
        val layoutWidth = if (cells.columnsTotalSize.weight == 0f) {
            min(constraints.maxWidth, cells.columnsTotalSize.fixed.roundToPx())
        } else {
            constraints.maxWidth
        }
        val layoutHeight = if (cells.rowsTotalSize.weight == 0f) {
            min(constraints.maxHeight, cells.rowsTotalSize.fixed.roundToPx())
        } else {
            constraints.maxHeight
        }
        layout(layoutWidth, layoutHeight) {
            placeables.forEachIndexed { index, placeable ->
                val contentMetaInfo = displayContent[index]
                val cellPlaceInfo = cellPlaces[contentMetaInfo.top][contentMetaInfo.left]
                placeable.placeRelative(x = cellPlaceInfo.x, y = cellPlaceInfo.y)
            }
        }
    }
}

@Preview
@Composable
fun GridPlacedPreview() = preview {
    GridPlaced()
}

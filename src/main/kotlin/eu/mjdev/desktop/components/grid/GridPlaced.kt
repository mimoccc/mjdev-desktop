package eu.mjdev.desktop.components.grid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.grid.base.*
import eu.mjdev.desktop.components.grid.base.Measure.calculateCellPlaces
import eu.mjdev.desktop.components.grid.base.Measure.measure
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import kotlin.math.min

@Composable
fun GridPlaced(
    cells: GridPlacedCells,
    modifier: Modifier = Modifier,
    placementPolicy: GridPlacedPlacementPolicy = GridPlacedPlacementPolicy.DEFAULT,
    guideVisible: Boolean = true,
    guideVisibleState: VisibilityState = rememberVisibilityState(guideVisible),
    guideLineColor: Color = Color.Black,
    guideLineSize: Dp = 1.dp,
    content: GridPlacedScope.() -> Unit
) {
    val scopeContent: GridPlacedScopeImpl = GridPlacedScopeImpl(cells, placementPolicy).apply(content)
    val displayContent: List<GridPlacedContent> = scopeContent.data.toList()
    Layout(
        modifier = modifier,
        content = {
            displayContent.forEach {
                it.item(GridPlacedItemScopeImpl)
            }
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

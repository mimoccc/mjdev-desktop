package eu.mjdev.desktop.components.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun MeasurableComponent(
    key: Any,
    onMeasure: (size: DpSize) -> Unit = {},
    content: @Composable () -> Unit = {},
) = SubcomposeLayout(
) { constraints ->
    subcompose(
        key,
        content
    ).map { measurable ->
        measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    }.first().let { placeable ->
        onMeasure(DpSize(placeable.width.dp, placeable.height.dp))
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }
}
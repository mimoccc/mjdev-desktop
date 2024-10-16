package eu.mjdev.desktop.components.custom

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.extensions.Compose.preview

@Composable
fun MeasureUnconstrainedView(
    viewToMeasure: @Composable () -> Unit,
    content: @Composable (DpSize) -> Unit,
) = SubcomposeLayout { constraints ->
    val measured = subcompose(
        "viewToMeasure",
        viewToMeasure
    ).getOrNull(0)?.measure(Constraints())
    val measuredWidth = measured?.width?.toDp() ?: 0.dp
    val measuredHeight = measured?.height?.toDp() ?: 0.dp
    val contentPlaceable = subcompose("content") {
        content(DpSize(measuredWidth, measuredHeight))
    }.getOrNull(0)?.measure(constraints)
    layout(
        contentPlaceable?.width ?: 0,
        contentPlaceable?.height ?: 0
    ) {
        contentPlaceable?.place(0, 0)
    }
}

@Preview
@Composable
fun MeasureUnconstrainedViewPreview() = preview {
    MeasureUnconstrainedView({
        TextAny("test")
    }, { size ->
        TextAny(
            modifier = Modifier.background(Color.White),
            text = "test size: $size"
        )
    })
}

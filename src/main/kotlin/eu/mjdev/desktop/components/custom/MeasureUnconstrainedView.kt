package eu.mjdev.desktop.components.custom

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import eu.mjdev.desktop.components.text.TextAny

@Composable
fun MeasureUnconstrainedView(
    viewToMeasure: @Composable () -> Unit,
    content: @Composable (DpSize) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val measured = subcompose("viewToMeasure", viewToMeasure)[0]
            .measure(Constraints())
        val measuredWidth = measured.width.toDp()
        val measuredHeight = measured.height.toDp()
        val contentPlaceable = subcompose("content") {
            content(DpSize(measuredWidth, measuredHeight))
        }[0].measure(constraints)
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.place(0, 0)
        }
    }
}

@Preview
@Composable
fun MeasureUnconstrainedViewPreview() = MeasureUnconstrainedView({
    TextAny("test")
}, { size ->
    TextAny("test size: $size")
})

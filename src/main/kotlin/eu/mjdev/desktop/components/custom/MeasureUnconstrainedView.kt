package eu.mjdev.desktop.components.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

@Composable
fun MeasureUnconstrainedView(
    viewToMeasure: @Composable () -> Unit,
    content: @Composable (measuredWidth: Dp, measuredHeight: Dp) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val measured = subcompose("viewToMeasure", viewToMeasure)[0]
            .measure(Constraints())
        val measuredWidth = measured.width.toDp()
        val measuredHeight = measured.height.toDp()
        val contentPlaceable = subcompose("content") {
            content(measuredWidth, measuredHeight)
        }[0].measure(constraints)
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.place(0, 0)
        }
    }
}

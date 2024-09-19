package eu.mjdev.desktop.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

@Suppress("FunctionName", "unused")
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

@Suppress("FunctionName", "unused")
@Composable
fun TestMeasurable() {
    MeasureUnconstrainedView(
        viewToMeasure = {
            Text("your sample text")
        }
    ) { mw, mh ->
        // measure view by another view
    }
}

package org.mjdev.desktop.components.desktop.widgets

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aay.compose.baseComponents.model.LegendPosition
import com.aay.compose.donutChart.model.PieChartData
import org.mjdev.desktop.components.chart.DonutChart
import org.mjdev.desktop.components.draggable.DraggableView
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.DoubleExt.toMemorySizeReadable
import org.mjdev.desktop.helpers.system.meminfo.MemInfo
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("FunctionName")
@Composable
fun MemoryChart(
    modifier: Modifier = Modifier,
    ratioLineColor: Color = Color.Transparent,
    mainTitle: String = "Memory",
    usedTitle: String = "Used",
    freeTitle: String = "Free",
    animationDuration: Int = 1000,
    animation: AnimationSpec<Float> = TweenSpec(durationMillis = animationDuration),
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    updateTimeOut: Long = 10000,
    dragEnabled: Boolean = true,
) = withDesktopContext {
    DraggableView(
        modifier = modifier,
        dragEnabled = dragEnabled
    ) {
        DonutChart(
            modifier = modifier,
            title = mainTitle,
            textColor = iconsTintColor,
            outerCircularColor = iconsTintColor,
            innerCircularColor = iconsTintColor,
            ratioLineColor = ratioLineColor,
            refreshTimeout = updateTimeOut,
            animation = animation,
            animationDuration = animationDuration,
            legendPosition = legendPosition
        ) {
            // todo
            MemInfo(context).let { mem ->
                val secondValue = mem.free
                val firstValue = mem.used
                val secondTitle = freeTitle.plus(": ").plus(mem.free.toMemorySizeReadable())
                val firstTitle = usedTitle.plus(": ").plus(mem.used.toMemorySizeReadable())
                listOf(
                    PieChartData(firstValue, iconsTintColor, firstTitle),
                    PieChartData(secondValue, backgroundColor, secondTitle)
                )
            }
        }
    }
}

// todo
@Preview
@Composable
fun MemoryChartPreview() = preview {
    MemoryChart()
}

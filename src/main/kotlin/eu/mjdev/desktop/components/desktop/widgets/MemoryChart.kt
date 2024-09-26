package eu.mjdev.desktop.components.desktop.widgets

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aay.compose.baseComponents.model.LegendPosition
import com.aay.compose.donutChart.model.PieChartData
import eu.mjdev.desktop.components.charts.DonutChart
import eu.mjdev.desktop.components.draggable.DraggableView
import eu.mjdev.desktop.helpers.internal.Palette.Companion.rememberBackgroundColor
import eu.mjdev.desktop.helpers.internal.Palette.Companion.rememberIconTintColor
import eu.mjdev.desktop.helpers.system.MemInfo
import eu.mjdev.desktop.helpers.system.MemInfo.Companion.toReadable
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("FunctionName")
@Composable
fun MemoryChart(
    modifier: Modifier = Modifier,
    ratioLineColor: Color = Color.Transparent,
    mainTitle: String = "Memory",
    usedTitle: String = "Used",
    freeTitle: String = "Free",
    animation: AnimationSpec<Float> = TweenSpec(durationMillis = 3000),
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    updateTimeOut: Long = 10000,
    dragEnabled: Boolean = true,
) = withDesktopScope {
    DraggableView(
        modifier = modifier,
        dragEnabled = dragEnabled
    ) {
        val backgroundColor by rememberBackgroundColor(api)
        val iconsTintColor by rememberIconTintColor(api)
        DonutChart(
            modifier = modifier,
            title = mainTitle,
            textColor = iconsTintColor,
            outerCircularColor = iconsTintColor,
            innerCircularColor = iconsTintColor,
            ratioLineColor = ratioLineColor,
            refreshTimeout = updateTimeOut,
            animation = animation,
            legendPosition = legendPosition
        ) {
            MemInfo().let { mem ->
                val secondValue = mem.free
                val firstValue = mem.used
                val secondTitle = freeTitle.plus(": ").plus(mem.free.toReadable())
                val firstTitle = usedTitle.plus(": ").plus(mem.used.toReadable())
                listOf(
                    PieChartData(firstValue, iconsTintColor, firstTitle),
                    PieChartData(secondValue, backgroundColor, secondTitle)
                )
            }
        }
    }
}

@Preview
@Composable
fun MemoryChartPreview() = MemoryChart()

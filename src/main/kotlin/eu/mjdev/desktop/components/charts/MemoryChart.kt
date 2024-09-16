package eu.mjdev.desktop.components.charts

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aay.compose.baseComponents.model.LegendPosition
import eu.mjdev.desktop.components.draggable.DraggableView
import eu.mjdev.desktop.extensions.Compose.DarkDarkGray
import eu.mjdev.desktop.helpers.system.MemInfo
import eu.mjdev.desktop.helpers.system.MemInfo.Companion.toReadable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MemoryChart(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    outerCircularColor: Color = Color.White,
    innerCircularColor: Color = Color.White,
    totalColor: Color = Color.DarkDarkGray,
    currentColor: Color = Color.White,
    ratioLineColor: Color = Color.Transparent,
    mainTitle: String = "Memory",
    usedTitle: String = "Used",
    freeTitle: String = "Free",
    animation: AnimationSpec<Float> = TweenSpec(durationMillis = 3000),
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    updateTimeOut: Long = 10000,
    dragEnabled: Boolean = true
) = DraggableView(
    modifier = modifier,
    dragEnabled = dragEnabled
) {
    val memData = remember { mutableStateOf(MemInfo()) }
    DualDonutChart(
        modifier = modifier,
        title = mainTitle,
        textColor = textColor,
        outerCircularColor = outerCircularColor,
        innerCircularColor = innerCircularColor,
        ratioLineColor = ratioLineColor,
        secondColor = totalColor,
        firstColor = currentColor,
        secondValue = memData.value.free,
        firstValue = memData.value.used,
        secondTitle = freeTitle.plus(": ").plus(memData.value.free.toReadable()),
        firstTitle = usedTitle.plus(": ").plus(memData.value.used.toReadable()),
        animation = animation,
        legendPosition = legendPosition
    )
    LaunchedEffect(Unit) {
        while (isActive) {
            MemInfo().let { n ->
                if (memData.value.percent != n.percent) {
                    memData.value = n
                }
            }
            delay(updateTimeOut)
        }
    }
}

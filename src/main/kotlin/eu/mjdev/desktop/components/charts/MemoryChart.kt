package eu.mjdev.desktop.components.charts

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aay.compose.baseComponents.model.LegendPosition
import eu.mjdev.desktop.components.draggable.DraggableView
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.helpers.internal.Palette.Companion.rememberBackgroundColor
import eu.mjdev.desktop.helpers.internal.Palette.Companion.rememberBorderColor
import eu.mjdev.desktop.helpers.internal.Palette.Companion.rememberIconTintColor
import eu.mjdev.desktop.helpers.internal.Palette.Companion.rememberTextColor
import eu.mjdev.desktop.helpers.system.MemInfo
import eu.mjdev.desktop.helpers.system.MemInfo.Companion.toReadable
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
    api: DesktopProvider = LocalDesktop.current,
) = DraggableView(
    modifier = modifier,
    dragEnabled = dragEnabled
) {
    val backgroundColor by rememberBackgroundColor(api)
    val textColor by rememberTextColor(api)
    val borderColor by rememberBorderColor(api)
    val iconsTintColor by rememberIconTintColor(api)
    val memData = rememberState(MemInfo())
    DualDonutChart(
        modifier = modifier,
        title = mainTitle,
        textColor = textColor,
        outerCircularColor = borderColor,
        innerCircularColor = borderColor,
        ratioLineColor = ratioLineColor,
        secondColor = backgroundColor,
        firstColor = iconsTintColor,
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

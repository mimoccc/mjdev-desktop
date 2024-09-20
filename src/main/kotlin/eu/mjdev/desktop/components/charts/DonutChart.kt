package eu.mjdev.desktop.components.charts

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.aay.compose.baseComponents.model.LegendPosition
import com.aay.compose.donutChart.DonutChart
import com.aay.compose.donutChart.model.PieChartData
import eu.mjdev.desktop.extensions.Compose.rememberState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Suppress("FunctionName")
@Composable
fun DonutChart(
    modifier: Modifier = Modifier,
    title: String = "",
    textColor: Color = Color.White,
    outerCircularColor: Color = Color.White,
    innerCircularColor: Color = Color.White,
    ratioLineColor: Color = Color.White,
    animationDuration: Int = 3000,
    refreshTimeout: Long = 5000L,
    animation: AnimationSpec<Float> = TweenSpec(durationMillis = animationDuration),
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    dataHandler: () -> List<PieChartData>
) {
    var data by rememberState(dataHandler())
    val textStyle by rememberState(TextStyle(color = textColor))
    DonutChart(
        modifier = modifier,
        pieChartData = data,
        centerTitle = title,
        centerTitleStyle = textStyle,
        descriptionStyle = textStyle,
        textRatioStyle = textStyle,
        outerCircularColor = outerCircularColor,
        innerCircularColor = innerCircularColor,
        ratioLineColor = ratioLineColor,
        animation = animation,
        legendPosition = legendPosition
    )
    LaunchedEffect(Unit) {
        while (isActive) {
            dataHandler().let { newData ->
                if (data != newData) {
                    data = newData
                }
            }
            delay(refreshTimeout)
        }
    }
}
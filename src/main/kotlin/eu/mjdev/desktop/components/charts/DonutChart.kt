package eu.mjdev.desktop.components.charts

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.aay.compose.baseComponents.model.LegendPosition
import com.aay.compose.donutChart.DonutChart
import com.aay.compose.donutChart.model.PieChartData
import eu.mjdev.desktop.extensions.Compose.preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Suppress("FunctionName")
@Composable
fun DonutChart(
    modifier: Modifier = Modifier,
    title: String = "",
    textColor: Color = Color.Black,
    outerCircularColor: Color = Color.Black,
    innerCircularColor: Color = Color.Black,
    ratioLineColor: Color = Color.Black,
    animationDuration: Int = 1,
    refreshTimeout: Long = 5000L,
    animation: AnimationSpec<Float> = TweenSpec(durationMillis = animationDuration),
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    dataHandler: () -> List<PieChartData> = { emptyList() }
) {
    var needRefresh by remember { mutableStateOf(0L) }
    val textStyle = TextStyle(color = textColor)
    val data by remember(
        title,
        textColor,
        outerCircularColor,
        innerCircularColor,
        ratioLineColor,
        animationDuration,
        refreshTimeout,
        animation,
        legendPosition,
        dataHandler,
        needRefresh
    ) { mutableStateOf(dataHandler()) }
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
        animation = TweenSpec(1),
        legendPosition = legendPosition
    )
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(refreshTimeout)
            needRefresh = System.currentTimeMillis()
        }
    }
}

// todo
@Preview
@Composable
fun DonutChartPreview() = preview(480) {
    DonutChart(
        textColor = Color.White,
        outerCircularColor = Color.White,
        innerCircularColor = Color.White,
        ratioLineColor = Color.White,
        title = "Donut Chart",
        legendPosition = LegendPosition.BOTTOM,
        dataHandler = {
            listOf(
                PieChartData(24.0, Color.Blue, "test1"),
                PieChartData(32.0, Color.Gray, "test2")
            )
        }
    )
}

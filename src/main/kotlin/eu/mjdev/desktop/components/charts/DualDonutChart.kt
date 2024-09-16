package eu.mjdev.desktop.components.charts

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.aay.compose.baseComponents.model.LegendPosition
import com.aay.compose.donutChart.DonutChart
import com.aay.compose.donutChart.model.PieChartData
import eu.mjdev.desktop.extensions.Compose.DarkDarkGray

@Composable
fun DualDonutChart(
    modifier: Modifier = Modifier,
    title: String = "",
    textColor: Color = Color.White,
    outerCircularColor: Color = Color.White,
    innerCircularColor: Color = Color.White,
    ratioLineColor: Color = Color.White,
    firstValue: Double = 0.0,
    secondValue: Double = 0.0,
    firstColor: Color = Color.DarkDarkGray,
    secondColor: Color = Color.White,
    firstTitle: String = "",
    secondTitle: String = "",
    animation: AnimationSpec<Float> = TweenSpec(durationMillis = 3000),
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
) {
    val data = remember(
        firstValue,
        firstColor,
        firstTitle,
        secondValue,
        secondColor,
        secondTitle
    ) {
        derivedStateOf {
            listOf(
                PieChartData(firstValue, firstColor, firstTitle),
                PieChartData(secondValue, secondColor, secondTitle)
            )
        }
    }
    DonutChart(
        modifier = modifier,
        pieChartData = data.value,
        centerTitle = title,
        centerTitleStyle = TextStyle(color = textColor),
        descriptionStyle = TextStyle(color = textColor),
        textRatioStyle = TextStyle(color = textColor),
        outerCircularColor = outerCircularColor,
        innerCircularColor = innerCircularColor,
        ratioLineColor = ratioLineColor,
        animation = animation,
        legendPosition = legendPosition
    )
}
package eu.mjdev.desktop.components.custom

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.text.TextWithShadow
import eu.mjdev.desktop.extensions.Custom.dateFlow
import eu.mjdev.desktop.extensions.Custom.timeFlow

@Preview
@Composable
fun Clock(
    modifier: Modifier = Modifier,
    timeTextSize: TextUnit = 32.sp,
    timeTextWeight: FontWeight = FontWeight.Bold,
    timeTextColor: Color = Color.White,
    dateTextSize: TextUnit = 16.sp,
    dateTextWeight: FontWeight = FontWeight.Bold,
    dateTextColor: Color = Color.White,
    showTime: Boolean = true,
    showDate: Boolean = true,
) = Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
) {
    Column {
        if (showTime) TextWithShadow(
            modifier = Modifier.fillMaxWidth(),
            text = timeFlow.value,
            textAlign = TextAlign.Center,
            fontWeight = timeTextWeight,
            fontSize = timeTextSize,
            color = timeTextColor
        )
        if (showDate) TextWithShadow(
            modifier = Modifier.fillMaxWidth(),
            text = dateFlow.value,
            textAlign = TextAlign.Center,
            fontWeight = dateTextWeight,
            fontSize = dateTextSize,
            color = dateTextColor
        )
    }
}

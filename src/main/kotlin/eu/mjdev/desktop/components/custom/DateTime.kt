package eu.mjdev.desktop.components.custom

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Compose.size

@Composable
fun DateTime(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    padding: Dp = 4.dp,
    timeTextSize: TextUnit = 32.sp,
    timeTextWeight: FontWeight = FontWeight.Bold,
    timeTextColor: Color = Color.Black,
    dateTextSize: TextUnit = 16.sp,
    dateTextWeight: FontWeight = FontWeight.Bold,
    dateTextColor: Color = Color.Black,
    showTime: Boolean = true,
    showDate: Boolean = true,
    talkEveryHour: Boolean = false,
    talkOnClick: Boolean = true
) = Box(
    modifier = modifier
        .background(backgroundColor)
        .padding(padding)
) {
    Clock(
        modifier = modifier,
        timeTextSize = timeTextSize,
        timeTextWeight = timeTextWeight,
        timeTextColor = timeTextColor,
        dateTextSize = dateTextSize,
        dateTextWeight = dateTextWeight,
        dateTextColor = dateTextColor,
        showTime = showTime,
        showDate = showDate,
        talkEveryHour = talkEveryHour,
        talkOnClick = talkOnClick
    )
}

// todo
@Preview
@Composable
fun DateTimePreview() = preview {
    DateTime(
        modifier = Modifier.size(320, 200)
    )
}

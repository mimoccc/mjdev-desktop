package eu.mjdev.desktop.components.custom

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun DateTime(
    backgroundColor: Color,
    textColor: Color = Color.White
) = Box(
    modifier = Modifier
        .fillMaxWidth()
        .background(backgroundColor)
        .padding(16.dp)
) {
    Clock(
        modifier = Modifier.fillMaxWidth(),
        timeTextColor = textColor,
        dateTextColor = textColor
    )
}
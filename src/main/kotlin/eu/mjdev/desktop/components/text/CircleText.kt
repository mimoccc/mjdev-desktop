package eu.mjdev.desktop.components.text

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Modifier.clipCircle

@Suppress("unused", "FunctionName")
@Composable
fun CircleText(
    modifier: Modifier = Modifier,
    backGroundColor: Color = MaterialTheme.colors.secondary,
    borderColor: Color = Color.White,
    borderSize: Dp = 2.dp,
    text: String = "0",
    textColor: Color = Color.White,
    textSize: TextUnit = 20.sp,
    contentPadding: Dp = 0.dp,
) = Box(
    modifier = modifier
        .size(textSize.value.dp * 2)
        .padding(contentPadding)
        .clipCircle()
        .background(backGroundColor, CircleShape)
        .border(
            BorderStroke(
                borderSize,
                borderColor
            ),
            CircleShape
        ),
    contentAlignment = Alignment.Center,
) {
    TextAny(
        modifier = Modifier,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = textColor,
        text = text,
        fontSize = textSize
    )
}

@Preview
@Composable
fun CircleTextPreview() = preview {
    CircleText()
}

package eu.mjdev.desktop.components.text

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

// todo may be need refactor
@Suppress("FunctionName")
@Preview
@Composable
fun AutoResizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
) = BoxWithConstraints(modifier = modifier) {
    Text(
        text = text,
        color = color,
        maxLines = Int.MAX_VALUE,
        fontSize = min(constraints.maxWidth, constraints.maxHeight).dp.minus(8.dp).value.sp,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Clip,
    )
}

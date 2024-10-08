package eu.mjdev.desktop.components.text

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Suppress("FunctionName")
@Composable
fun TextWithShadow(
    modifier: Modifier = Modifier,
    shadowSize: Dp = 4.dp,
    shadowColor: Color = Color.Black.copy(alpha = 0.5f),
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign? = null,
    fontSize: TextUnit = 16.sp,
    color: Color = Color.Red,
    overflow: TextOverflow = Ellipsis,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    text: Any? = "test"
) = TextAny(
    modifier = modifier,
    style = MaterialTheme.typography.body1.copy(
        shadow = Shadow(
            color = shadowColor,
            offset = Offset(offsetX.value, offsetY.value),
            blurRadius = shadowSize.value
        )
    ),
    text = text,
    textAlign = textAlign,
    fontWeight = fontWeight,
    fontSize = fontSize,
    color = color,
    overflow = overflow,
    minLines = minLines,
    maxLines = maxLines
)

@Preview
@Composable
fun TextWithShadowPreview() = TextWithShadow()

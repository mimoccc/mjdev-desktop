package org.mjdev.desktop.components.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.extensions.Compose.preview
import kotlin.math.min
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.max

// todo may be need refactor
@Suppress("FunctionName")
@Composable
fun AutoResizeText(
    text: String = "A",
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = FontStyle.Normal,
    fontWeight: FontWeight? = FontWeight.Bold,
    fontFamily: FontFamily? = FontFamily.Default,
    maxLines: Int = 1,
    contentAlignment: Alignment = Alignment.Center,
    textAlign: TextAlign = TextAlign.Center,
    minFontSize: TextUnit = 16.sp
) = BoxWithConstraints(
    modifier = modifier,
    contentAlignment = contentAlignment
) {
    val maxFontSize = minOf(maxWidth, maxHeight)
    val calculatedFontSize = min(
        maxFontSize.value,
        max(
            minFontSize.value,
            constraints
                .maxWidth
                .coerceAtMost(
                    constraints.maxHeight
                ).toFloat() / 2
        )
    ).sp
    Text(
        modifier = modifier,
        text = text,
        color = color,
        fontStyle = fontStyle,
        fontSize = calculatedFontSize,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        maxLines = maxLines,
        textAlign = textAlign,
        overflow = TextOverflow.Clip,
    )
}

@Preview
@Composable
fun AutoResizeTextPreview() = preview(64, 64) {
    AutoResizeText(
        modifier = Modifier
            .background(Color.White)
            .size(64.dp),
        text = "o",
    )
}

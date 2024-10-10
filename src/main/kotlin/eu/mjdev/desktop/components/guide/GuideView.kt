/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.guide

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GuideLines(
    modifier: Modifier = Modifier,
    rows: Int = 10,
    columns: Int = 10,
    color: Color = Color.Black,
    lineSize: Dp = 1.dp,
    visible: Boolean = true,
) = Canvas(
    modifier = modifier
) {
    (0..columns).forEach { i ->
        val x = i * size.width / columns
        drawLine(
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            color = if (visible) color else Color.Transparent,
            strokeWidth = lineSize.toPx(),
            cap = StrokeCap.Round
        )
    }
    (0..rows).forEach { i ->
        val y = i * size.height / rows
        drawLine(
            start = Offset(0f, y),
            end = Offset(size.width, y),
            color = if (visible) color else Color.Transparent,
            strokeWidth = lineSize.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Preview
@Composable
fun GuideLinesPreview() = GuideLines(modifier = Modifier.fillMaxSize())

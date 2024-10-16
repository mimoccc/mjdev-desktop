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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Compose.preview
import kotlin.math.max

@Composable
fun GuideLines(
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    lineSize: Dp = 1.dp,
    visible: Boolean = true,
    cellSize: DpSize = DpSize(10.dp, 10.dp)
) = Canvas(
    modifier = modifier
) {
    val rows = max(1, size.height.div(cellSize.height.value).toInt())
    val columns = max(1, size.width.div(cellSize.width.value).toInt())
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
fun GuideLinesPreview() = preview(320, 320) {
    GuideLines(
        modifier = Modifier.fillMaxSize(),
        cellSize = DpSize(32.dp, 32.dp),
        color = Color.White
    )
}

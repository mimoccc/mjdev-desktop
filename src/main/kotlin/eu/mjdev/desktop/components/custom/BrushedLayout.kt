/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.custom

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun BrushedLayout(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    colorStart: Color = Color.Black.copy(alpha = if (alpha.isNaN()) 1f else alpha),
    colorEnd: Color = Color.Transparent,
) {
    val brush = Brush.radialGradient(listOf(colorStart, colorEnd))
    Canvas(
        modifier = modifier,
        onDraw = {
            drawRect(brush)
        }
    )
}

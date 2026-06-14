/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.custom

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.extensions.Compose.preview

@Composable
fun BrushedLayout(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    colorStart: Color = Color.Black.copy(alpha = if (alpha.isNaN()) 1f else alpha),
    colorEnd: Color = Color.Transparent,
    brush: Brush = Brush.radialGradient(listOf(colorStart, colorEnd)),
) = Canvas(
    modifier = modifier,
    onDraw = {
        drawRect(brush)
    },
)

@Preview
@Composable
fun PreviewBrushedLayout() =
    preview(320) {
        BrushedLayout()
    }

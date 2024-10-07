/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.icons.indicator

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.icons.Icons
import kotlin.Suppress

@Suppress("unused")
val Icons.RunningIndicator: ImageVector by lazy {
    ImageVector.Builder(
        name = "RunningIndicator",
        defaultWidth = 119.98.dp,
        defaultHeight = 119.98.dp,
        viewportWidth = 119.98f,
        viewportHeight = 119.98f
    ).apply {
        path(
            fill = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to Color(0xFFFFFFFF),
                    1f to Color(0x00FFFFFF),
                    0f to Color(0xFFFFFFFF),
                    1f to Color(0x00FFFFFF)
                ),
                center = Offset(59.3f, 113.65f),
                radius = 60.68f
            ),
            fillAlpha = 0.372f,
            strokeAlpha = 0.372f
        ) {
            moveTo(8.19f, 2.59f)
            lineTo(112.27f, 2.59f)
            arcTo(
                5.61f, 5.61f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 117.88f, 8.2f
            )
            lineTo(117.88f, 112.28f)
            arcTo(
                5.61f, 5.61f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 112.27f, 117.89f
            )
            lineTo(8.19f, 117.89f)
            arcTo(
                5.61f, 5.61f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 2.58f, 112.28f
            )
            lineTo(2.58f, 8.2f)
            arcTo(
                5.61f, 5.61f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 8.19f, 2.59f
            )
            close()
        }
    }.build()
}

/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.icons.toggle

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.icons.Icons

val Icons.ToggleOnLight: ImageVector by lazy {
    ImageVector.Builder(
        name = "ToggleOnLight",
        defaultWidth = 46.dp,
        defaultHeight = 26.dp,
        viewportWidth = 46f,
        viewportHeight = 26f
    ).apply {
        path(
            fill = SolidColor(Color(0xFF666C66)),
            stroke = SolidColor(Color(0x00000000)),
            strokeLineWidth = 1f
        ) {
            moveTo(12.458f, 0f)
            lineTo(33.542f, 0f)
            arcTo(
                12.458f, 13f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 46f, 13f
            )
            lineTo(46f, 13f)
            arcTo(
                12.458f, 13f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 33.542f, 26f
            )
            lineTo(12.458f, 26f)
            arcTo(
                12.458f, 13f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 0f, 13f
            )
            lineTo(0f, 13f)
            arcTo(
                12.458f, 13f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 12.458f, 0f
            )
            close()
        }
        path(
            fill = SolidColor(Color(0xFF000000)),
            fillAlpha = 0.2f,
            stroke = SolidColor(Color(0x00000000)),
            strokeLineWidth = 1f
        ) {
            moveTo(33f, 4f)
            lineTo(33f, 4f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 43f, 14f
            )
            lineTo(43f, 14f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 33f, 24f
            )
            lineTo(33f, 24f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 23f, 14f
            )
            lineTo(23f, 14f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 33f, 4f
            )
            close()
        }
        path(
            fill = SolidColor(Color(0xFFFFFFFF)),
            stroke = SolidColor(Color(0x00000000)),
            strokeLineWidth = 1f
        ) {
            moveTo(33f, 3f)
            lineTo(33f, 3f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 43f, 13f
            )
            lineTo(43f, 13f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 33f, 23f
            )
            lineTo(33f, 23f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 23f, 13f
            )
            lineTo(23f, 13f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = true, 33f, 3f
            )
            close()
        }
    }.build()
}


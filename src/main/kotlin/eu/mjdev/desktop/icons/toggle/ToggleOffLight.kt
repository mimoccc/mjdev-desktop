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
import kotlin.Suppress

@Suppress("unused")
val Icons.ToggleOffLight: ImageVector by lazy {
    ImageVector.Builder(
        name = "ToggleOffLight",
        defaultWidth = 46.dp,
        defaultHeight = 26.dp,
        viewportWidth = 46f,
        viewportHeight = 26f
    ).apply {
        path(
            fill = SolidColor(Color(0xFFBABABA)),
            stroke = SolidColor(Color(0x00000000)),
            strokeLineWidth = 1f
        ) {
            moveTo(33.542f, 0f)
            lineTo(12.458f, 0f)
            arcTo(
                13f, 12.458f, 90f,
                isMoreThanHalf = false, isPositiveArc = false, 0f, 13f
            )
            lineTo(0f, 13f)
            arcTo(
                13f, 12.458f, 90f,
                isMoreThanHalf = false, isPositiveArc = false, 12.458f, 26f
            )
            lineTo(33.542f, 26f)
            arcTo(
                13f, 12.458f, 90f,
                isMoreThanHalf = false, isPositiveArc = false, 46f, 13f
            )
            lineTo(46f, 13f)
            arcTo(
                13f, 12.458f, 90f,
                isMoreThanHalf = false, isPositiveArc = false, 33.542f, 0f
            )
            close()
        }
        path(
            fill = SolidColor(Color(0xFF000000)),
            fillAlpha = 0.2f,
            stroke = SolidColor(Color(0x00000000)),
            strokeLineWidth = 1f
        ) {
            moveTo(13f, 4f)
            lineTo(13f, 4f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = false, 3f, 14f
            )
            lineTo(3f, 14f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = false, 13f, 24f
            )
            lineTo(13f, 24f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = false, 23f, 14f
            )
            lineTo(23f, 14f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = false, 13f, 4f
            )
            close()
        }
        path(
            fill = SolidColor(Color(0xFFF8F7F7)),
            stroke = SolidColor(Color(0x00000000)),
            strokeLineWidth = 1f
        ) {
            moveTo(13f, 3f)
            lineTo(13f, 3f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = false, 3f, 13f
            )
            lineTo(3f, 13f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = false, 13f, 23f
            )
            lineTo(13f, 23f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = false, 23f, 13f
            )
            lineTo(23f, 13f)
            arcTo(
                10f, 10f, 0f,
                isMoreThanHalf = false, isPositiveArc = false, 13f, 3f
            )
            close()
        }
    }.build()
}

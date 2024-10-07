/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.icons.checkbox

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.icons.Icons
import kotlin.Suppress

@Suppress("unused")
val Icons.CheckboxFocusedLight: ImageVector by lazy {
    ImageVector.Builder(
        name = "CheckboxFocusedLight",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color(0xFF444C44)),
            stroke = SolidColor(Color(0xFF666C66)),
            strokeLineWidth = 2f,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(4.385f, 1f)
            lineTo(19.615f, 1f)
            arcTo(
                3.385f, 3.385f, 0f,
                isMoreThanHalf = false,
                isPositiveArc = true, 23f, 4.385f
            )
            lineTo(23f, 19.615f)
            arcTo(
                3.385f, 3.385f, 0f,
                isMoreThanHalf = false,
                isPositiveArc = true, 19.615f, 23f
            )
            lineTo(4.385f, 23f)
            arcTo(
                3.385f, 3.385f, 0f,
                isMoreThanHalf = false,
                isPositiveArc = true, 1f, 19.615f
            )
            lineTo(1f, 4.385f)
            arcTo(
                3.385f, 3.385f, 0f,
                isMoreThanHalf = false,
                isPositiveArc = true, 4.385f, 1f
            )
            close()
        }
        path(
            fill = SolidColor(Color(0xFFFFFFFF)),
            strokeLineWidth = 0.5f,
            strokeLineCap = StrokeCap.Square,
            pathFillType = PathFillType.EvenOdd
        ) {
            moveToRelative(17.471f, 6.776f)
            lineToRelative(1.571f, 1.571f)
            lineToRelative(-8.861f, 8.861f)
            lineToRelative(-5.33f, -5.332f)
            lineToRelative(1.571f, -1.571f)
            lineToRelative(3.759f, 3.759f)
            close()
        }
    }.build()
}


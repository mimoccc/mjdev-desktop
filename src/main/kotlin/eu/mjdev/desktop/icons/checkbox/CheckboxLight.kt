/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.icons.checkbox

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.icons.Icons

@Suppress("unused")
val Icons.CheckboxLight: ImageVector by lazy {
    ImageVector.Builder(
        name = "CheckboxLight",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color(0xFF444C44)),
            stroke = SolidColor(Color(0xFF666C66)),
            strokeLineWidth = 1f,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(4.731f, 1.5f)
            lineTo(19.269f, 1.5f)
            arcTo(3.231f, 3.231f, 0f,
                isMoreThanHalf = false,
                isPositiveArc = true, 22.5f, 4.731f)
            lineTo(22.5f, 19.269f)
            arcTo(3.231f, 3.231f, 0f,
                isMoreThanHalf = false,
                isPositiveArc = true, 19.269f, 22.5f)
            lineTo(4.731f, 22.5f)
            arcTo(3.231f, 3.231f, 0f,
                isMoreThanHalf = false,
                isPositiveArc = true, 1.5f, 19.269f)
            lineTo(1.5f, 4.731f)
            arcTo(3.231f, 3.231f, 0f,
                isMoreThanHalf = false,
                isPositiveArc = true, 4.731f, 1.5f)
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


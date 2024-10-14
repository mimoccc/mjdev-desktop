/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.icons.wifi.level

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.icons.Icons

val Icons.Wifi_Level_5: ImageVector by
        lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Wifi_Level_5",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).apply {
        path(fill = SolidColor(Color(0xFFE8EAED))) {
            moveTo(480f, 840f)
            lineTo(0f, 360f)
            quadToRelative(95f, -97f, 219.5f, -148.5f)
            reflectiveQuadTo(480f, 160f)
            quadToRelative(136f, 0f, 260.5f, 51.5f)
            reflectiveQuadTo(960f, 360f)
            lineTo(480f, 840f)
            close()
        }
    }.build()
}

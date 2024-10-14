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

val Icons.Wifi_Level_1: ImageVector by
lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Wifi_Level_1",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).apply {
        path(fill = SolidColor(Color(0xFFE8EAED))) {
            moveTo(480f, 840f)
            lineTo(0f, 360f)
            quadToRelative(96f, -98f, 220f, -149f)
            reflectiveQuadToRelative(260f, -51f)
            quadToRelative(137f, 0f, 261f, 51f)
            reflectiveQuadToRelative(219f, 149f)
            lineTo(480f, 840f)
            close()
            moveTo(361f, 607f)
            quadToRelative(25f, -18f, 55.5f, -28f)
            reflectiveQuadToRelative(63.5f, -10f)
            quadToRelative(33f, 0f, 63.5f, 10f)
            reflectiveQuadToRelative(55.5f, 28f)
            lineToRelative(245f, -245f)
            quadToRelative(-78f, -59f, -170.5f, -90.5f)
            reflectiveQuadTo(480f, 240f)
            quadToRelative(-101f, 0f, -193.5f, 31.5f)
            reflectiveQuadTo(116f, 362f)
            lineToRelative(245f, 245f)
            close()
        }
    }.build()
}

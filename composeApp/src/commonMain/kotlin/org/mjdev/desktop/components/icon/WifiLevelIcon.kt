/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package  org.mjdev.desktop.components.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.MutableStateExt.rememberComputed
import org.mjdev.desktop.icons.wifi.level.Wifi_Level_0
import org.mjdev.desktop.icons.wifi.level.Wifi_Level_1
import org.mjdev.desktop.icons.wifi.level.Wifi_Level_2
import org.mjdev.desktop.icons.wifi.level.Wifi_Level_3
import org.mjdev.desktop.icons.wifi.level.Wifi_Level_4
import org.mjdev.desktop.icons.wifi.level.Wifi_Level_5
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WifiLevelIcon(
    modifier: Modifier = Modifier,
    level: Int,
    color: Color = Color.Black,
    iconSize: DpSize = DpSize(24.dp, 24.dp)
) {
    val levelIcon by rememberComputed(level, iconSize) {
        when {
            level > 80 -> Wifi_Level_5
            level > 60 -> Wifi_Level_4
            level > 40 -> Wifi_Level_3
            level > 20 -> Wifi_Level_2
            level > 10 -> Wifi_Level_1
            else -> Wifi_Level_0
        }
    }
    Icon(
        modifier = modifier.size(iconSize),
        imageVector = levelIcon,
        contentDescription = "",
        tint = color
    )
}

@Preview
@Composable
fun PreviewWifiLevelIcon() = preview {
    WifiLevelIcon(
        level = 55,
        color = Color.White
    )
}

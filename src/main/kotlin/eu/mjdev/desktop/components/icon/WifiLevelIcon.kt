/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package  eu.mjdev.desktop.components.icon

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.composables.core.Icon
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Compose.rememberComputed
import eu.mjdev.desktop.icons.Icons
import eu.mjdev.desktop.icons.wifi.level.*

@Composable
fun WifiLevelIcon(
    modifier: Modifier = Modifier,
    level: Int,
    color: Color = Color.Black,
    iconSize: DpSize = DpSize(24.dp, 24.dp)
) {
    val levelIcon by rememberComputed(level, iconSize) {
        when {
            level > 80 -> Icons.Wifi_Level_5
            level > 60 -> Icons.Wifi_Level_4
            level > 40 -> Icons.Wifi_Level_3
            level > 20 -> Icons.Wifi_Level_2
            level > 10 -> Icons.Wifi_Level_1
            else -> Icons.Wifi_Level_0
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
fun WifiLevelIconPreview() = preview {
    WifiLevelIcon(
        level = 55,
        color = Color.White
    )
}

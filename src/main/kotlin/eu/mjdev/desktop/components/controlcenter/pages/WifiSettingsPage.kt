package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.data.WifiInfo
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.extensions.Custom.flowBlock
import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.helpers.system.Shell.Companion.CMD_NMCLI
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import eu.mjdev.desktop.components.icon.WifiLevelIcon

// nmcli -t -f ALL dev wifi
// nmcli device show
@Suppress("FunctionName")
fun WifiSettingsPage() = ControlCenterPage(
    icon = Icons.Filled.Wifi,
    name = "Wifi",
    condition = { connectionManager.isWifiAdapterAvailable }
) {
    var lastSeen by rememberState(0L)
    val list by flowBlock {
        Shell.executeAndReadLines(CMD_NMCLI, "-t", "-f", "ALL", "dev", "wifi").map { ws ->
            ws.split(":")
        }
    }.flowOn(Dispatchers.IO).collectAsState(emptyList())
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            list.forEach { wr ->
                item {
                    WifiRow(WifiInfo(wr))
                }
            }
        }
    }
    launchedEffect(lastSeen) {
        lastSeen = System.currentTimeMillis()
        delay(500L)
    }
}

@Composable
fun WifiRow(
    info: WifiInfo
) = withDesktopScope {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextAny(
            modifier = Modifier.weight(1f),
            text = info.ssid,
            color = textColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            singleLine = true,
            textSelectionEnabled = false
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextAny(
                modifier = Modifier.padding(start = 8.dp).wrapContentWidth(),
                text = info.encryption,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Start,
                singleLine = true,
                textSelectionEnabled = false
            )
            WifiLevelIcon(
                modifier = Modifier.padding(start = 8.dp).size(28.dp),
                color = textColor,
                level = info.signalLevel.toInt()
            )
            RadioButton(
                modifier = Modifier.padding(start = 8.dp).size(24.dp),
                selected = true,
                onClick = {},
                colors = RadioButtonDefaults.colors(
                    selectedColor = textColor,
                    unselectedColor = textColor,
                    disabledColor = textColor.alpha(0.5f)
                )
            )
        }
    }
}

@Preview
@Composable
fun WifiSettingsPagePreview() = WifiSettingsPage().render()

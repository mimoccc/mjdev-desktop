package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import eu.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.helpers.system.Shell.Companion.CMD_NMCLI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

// nmcli -t -f ALL dev wifi
// nmcli device show
@Suppress("FunctionName")
fun WifiSettingsPage() = ControlCenterPage(
    icon = Icons.Filled.Wifi,
    name = "Wifi",
    condition = { connectionManager.isWifiAdapterAvailable }
) {
    var lastSeen by rememberState(0L)
    val list by flow<List<List<String>>> {
        Shell.executeAndReadLines(
            CMD_NMCLI, "-t", " -f", "ALL", "dev", "wifi"
        ).map { ws ->
            println(ws)
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
                    WifiRow(wr)
                }
            }
        }
    }
    launchedEffect(lastSeen) {
        lastSeen = System.currentTimeMillis()
        delay(500L)
    }
}

// 0  : AP[n]
// 1  : ssid
// 2  : ? 57692D737472656C
// 3  : IPV6 or IP
// 4  : ? Infra
// 5  : freq type
// 6  : speed
// 7  : freq
// 8  : level in db
// 9  : level semi-graph
// 10 : encryption
// 11 : ?
// 12 : device
// 14 : ?
// 15 : connected [*  || ""]
// 16 : config point
@Composable
fun WifiRow(item: List<String>) = Row(
    modifier = Modifier.fillMaxWidth(),
) {
    TextAny(
        modifier = Modifier.weight(1f),
        text = item.getOrNull(1).orEmpty(),
        color = Color.White,
        textAlign = TextAlign.Start,
        textSelectionEnabled = true
    )
    TextAny(
        modifier = Modifier.wrapContentWidth(),
        text = item.getOrNull(9).orEmpty(),
        color = Color.White,
        textAlign = TextAlign.Start,
        textSelectionEnabled = true
    )
}

@Preview
@Composable
fun WifiSettingsPagePreview() = WifiSettingsPage().render()

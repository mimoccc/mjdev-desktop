package org.mjdev.desktop.components.controlcenter.pages.wifi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPageScope.Companion.rememberComputed
import org.mjdev.desktop.components.controlcenter.base.PersistentPageSaver
import org.mjdev.desktop.components.list.ExpandableLazyColumn
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.data.WifiNetwork
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.LaunchedEffect.flowBlock
import org.mjdev.desktop.icons.settings.SettingsWifi
import org.mjdev.desktop.log.Log

@Suppress("FunctionName")
fun WifiSettingsPage(context: IDesktopContext) =
    ControlCenterPage(
        context = context,
        icon = SettingsWifi,
        name = "Wifi",
        condition = {
            connectionManager.isWifiAdapterAvailable
        },
        saver = { ctx ->
            PersistentPageSaver(ctx, "Assistant")
        },
    ) {
        val shape = RoundedCornerShape(8.dp)
        var isConnecting by mutableStateOf(false)
        val wifiList: Map<String, WifiNetwork> by flowBlock(
            emptyMap(),
            250L,
        ) { context.connectionManager.wifiNetworks }
        val items: List<WifiNetwork> by rememberComputed(isConnecting) {
            if (wifiList.isEmpty()) {
                emptyList()
            } else {
                wifiList.map { e -> e.value }
            }
        }
        val connect: (item: WifiNetwork) -> Unit =
            remember {
                { wn ->
                    isConnecting = true
                    context.connectionManager
                        .connectWifi(wn.ssid)
                        .onSuccess {
                            Log.d("Connected to ${wn.ssid}")
                            isConnecting = false
                        }.onFailure { e ->
                            Log.e(e)
                            isConnecting = false
                        }
                }
            }

        ExpandableLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            items = items,
        ) { idx, item, expandedState ->
            WifiRow(
                modifier =
                    Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            shape,
                        ).border(
                            2.dp,
                            if (item.isActive) textColor else borderColor,
                            shape,
                        ).padding(8.dp),
                idx = idx,
                item = item,
                expandedState = expandedState,
                isConnecting = isConnecting,
                connect = {
                    connect(item)
                },
            )
        }
    }

@Preview
@Composable
fun WifiSettingsPagePreview() =
    preview {
        WifiSettingsPage(context).Render()
    }

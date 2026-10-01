package org.mjdev.desktop.components.controlcenter.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mjdev.desktop.data.NetDetail
import org.mjdev.desktop.data.NetDevice
import org.mjdev.desktop.managers.connectivity.IConnectivityManager

/** Constants of [SettingsNetDeviceSection]. */
private object NetDeviceSectionDefaults {
    const val CONNECTED_LABEL = "Connected"

    /** Period in milliseconds between two reads of the device details. */
    const val REFRESH_MS = 5_000L
}

/**
 * Settings for one network [device]: a switch that connects or disconnects it, followed by its
 * current details (connection, IP address, gateway, DNS, MAC). Details refresh in the background.
 */
@Suppress("FunctionName")
@Composable
fun SettingsNetDeviceSection(
    device: NetDevice,
    manager: IConnectivityManager,
) {
    val scope = rememberCoroutineScope()
    var details by remember(device.name) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var busy by remember(device.name) { mutableStateOf(false) }
    LaunchedEffect(device.name) {
        while (isActive) {
            details = withContext(Dispatchers.Default) { manager.deviceDetails(device.name) }
            delay(NetDeviceSectionDefaults.REFRESH_MS)
        }
    }
    SettingsSection(title = device.name) {
        SettingsSwitchRow(
            label = NetDeviceSectionDefaults.CONNECTED_LABEL,
            checked = NetDetail.isConnected(details),
            enabled = !busy,
            onCheckedChange = { connect ->
                scope.launch {
                    busy = true
                    withContext(Dispatchers.Default) { manager.setDeviceConnected(device.name, connect) }
                    details = withContext(Dispatchers.Default) { manager.deviceDetails(device.name) }
                    busy = false
                }
            },
        )
        NetDetail.entries
            .filter { it != NetDetail.State }
            .forEach { detail ->
                details[detail.key]?.let { value -> SettingsInfoRow(detail.label, value) }
            }
    }
}

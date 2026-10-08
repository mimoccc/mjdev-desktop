package org.psippr.desktop.components.controlcenter.pages.bluetooth

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.psippr.desktop.components.controlcenter.base.ControlCenterPage
import org.psippr.desktop.components.controlcenter.settings.SettingsInfoRow
import org.psippr.desktop.components.controlcenter.settings.SettingsPageColumn
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.components.controlcenter.settings.SettingsSwitchRow
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.data.BthDevice
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.icons.network.Bluetooth

/** Static labels used by [BluetoothSettingsPage]. */
private object BluetoothLabels {
    const val ADAPTER = "Adapter"
    const val POWER = "Bluetooth on"
    const val DEVICES = "Devices"
    const val NONE = "No devices found. Pair a device with the system tools first."
    const val PAIRED = " (paired)"
}

/** Period in milliseconds between two reads of the bluetooth state. */
private const val REFRESH_MS = 5_000L

/**
 * Control center page for bluetooth: switch the adapter on or off and connect or disconnect the
 * known devices. Hidden when the machine has no bluetooth adapter.
 */
@Suppress("FunctionName")
fun BluetoothSettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = Icons.Filled.Bluetooth,
    name = "Bluetooth",
    condition = {
        connectionManager.isBthAdapterAvailable
    },
) {
    val manager = context.connectionManager
    val scope = rememberCoroutineScope()
    var powered by remember { mutableStateOf(false) }
    var devices by remember { mutableStateOf<List<BthDevice>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }

    suspend fun reload() = withContext(Dispatchers.Default) {
        powered = manager.isBluetoothPowered
        devices = manager.bthNetworks.values.sortedWith(compareBy({ !it.connected }, { it.name }))
    }
    LaunchedEffect(Unit) {
        while (isActive) {
            reload()
            delay(REFRESH_MS)
        }
    }
    SettingsPageColumn {
        SettingsSection(title = BluetoothLabels.ADAPTER) {
            SettingsSwitchRow(
                label = BluetoothLabels.POWER,
                checked = powered,
                enabled = !busy,
                onCheckedChange = { on ->
                    scope.launch {
                        busy = true
                        withContext(Dispatchers.Default) { manager.setBluetoothPowered(on) }
                        reload()
                        busy = false
                    }
                },
            )
        }
        SettingsSection(title = BluetoothLabels.DEVICES) {
            if (devices.isEmpty()) {
                SettingsInfoRow(label = BluetoothLabels.NONE, value = "")
            }
            devices.forEach { device ->
                SettingsSwitchRow(
                    label = device.name + if (device.paired) BluetoothLabels.PAIRED else "",
                    checked = device.connected,
                    enabled = powered && !busy,
                    onCheckedChange = { connect ->
                        scope.launch {
                            busy = true
                            withContext(Dispatchers.Default) {
                                manager.setBluetoothDeviceConnected(device.address, connect)
                            }
                            reload()
                            busy = false
                        }
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun BluetoothSettingsPagePreview() = preview {
    BluetoothSettingsPage(context).Render()
}

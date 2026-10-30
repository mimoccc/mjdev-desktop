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
    // old: sent the user to other tools, the page itself could not find new devices
//    const val NONE = "No devices found. Pair a device with the system tools first."
    const val NONE = "No devices found yet."
    const val SEARCHING = "Searching for devices nearby…"
    const val OFF = "Switch bluetooth on to see devices."
    const val PAIRED = " (paired)"
    const val NEW = " (new)"
}

/** Period in milliseconds between two reads of the bluetooth state. */
private const val REFRESH_MS = 5_000L

/**
 * Control center page for bluetooth: switch the adapter on or off, search for devices nearby
 * while it is on, and connect or disconnect them (a new device is paired on its first connect).
 * Hidden when the machine has no bluetooth adapter.
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
    // old: only re-read the devices bluez already knew, a new device never showed up
//    LaunchedEffect(Unit) {
//        while (isActive) {
//            reload()
//            delay(REFRESH_MS)
//        }
//    }
    var searching by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (isActive) {
            reload()
            if (powered && !busy) {
                // a search blocks for its whole duration, the list refreshes right after it
                searching = true
                withContext(Dispatchers.Default) { manager.scanBluetooth() }
                searching = false
                reload()
            } else {
                delay(REFRESH_MS)
            }
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
            // old: one fixed hint, also while switched off or searching
//            if (devices.isEmpty()) {
//                SettingsInfoRow(label = BluetoothLabels.NONE, value = "")
//            }
            when {
                !powered -> SettingsInfoRow(label = BluetoothLabels.OFF, value = "")
                searching -> SettingsInfoRow(label = BluetoothLabels.SEARCHING, value = "")
                devices.isEmpty() -> SettingsInfoRow(label = BluetoothLabels.NONE, value = "")
            }
            devices.forEach { device ->
                SettingsSwitchRow(
                    // old: new devices had no mark, switching one on only tried to connect it
//                    label = device.name + if (device.paired) BluetoothLabels.PAIRED else "",
                    label = device.name + if (device.paired) BluetoothLabels.PAIRED else BluetoothLabels.NEW,
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

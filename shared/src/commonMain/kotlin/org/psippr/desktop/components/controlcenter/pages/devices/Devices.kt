package org.psippr.desktop.components.controlcenter.pages.devices

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.psippr.desktop.components.controlcenter.base.ControlCenterPage
import org.psippr.desktop.components.controlcenter.settings.SettingsDeviceSection
import org.psippr.desktop.components.controlcenter.settings.SettingsPageColumn
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.components.text.TextAny
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.data.NetDevice
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.LaunchedEffect.flowBlock
import org.psippr.desktop.icons.mobile.MobileFriendly
import org.psippr.desktop.managers.devices.ConnectedDevice

// old: hidden unless a device was there when the control center was built (the condition is
// read once), and phones were shown as network interfaces with meaningless Up/Down and MAC
// /** Title of the connected devices section. */
// private const val CONNECTED = "Connected devices"
//
// /** Refresh period of the device list in milliseconds. */
// private const val REFRESH_MS = 1000L
//
// /** Control center page listing currently connected devices; hidden when there are none. */
// @Suppress("FunctionName")
// fun DevicesPage(context: IDesktopContext) = ControlCenterPage(
//     context = context,
//     icon = Icons.Filled.Devices,
//     name = "Connected devices",
//     condition = {
//         connectionManager.hasConnectedDevices
//     },
// ) {
//     val devices: Map<String, NetDevice> by flowBlock(
//         emptyMap(),
//         REFRESH_MS,
//     ) { context.connectionManager.connectedDevices.toMap() }
//     SettingsPageColumn {
//         SettingsDeviceSection(CONNECTED, devices.values)
//     }
// }

/**
 * Control center tab of the connected devices (phones, tablets, ...): their facts and actions,
 * from every provider of [org.psippr.desktop.managers.devices.IDevicesManager] (ADB first). Always
 * shown: a device plugged in later must appear without restarting the desktop.
 */
@Suppress("FunctionName")
fun DevicesPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = Icons.Filled.Devices,
    name = DevicesPageDefaults.PAGE_NAME,
    condition = { true },
) {
    val manager = context.devicesManager
    var devices by remember { mutableStateOf<List<ConnectedDevice>>(emptyList()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            devices = withContext(Dispatchers.IO) { manager.devices() }
            delay(DevicesPageDefaults.REFRESH_MS)
        }
    }
    SettingsPageColumn {
        if (devices.isEmpty()) {
            SettingsSection(title = DevicesPageDefaults.NONE_TITLE) {
                TextAny(
                    text = DevicesPageDefaults.NONE_HINT,
                    color = textColor,
                    fontSize = 12.sp,
                )
            }
        }
        devices.forEach { device -> DeviceSection(device = device, manager = manager) }
    }
}

@Preview
@Composable
fun DevicesPagePreview() = preview {
    DevicesPage(context).Render()
}

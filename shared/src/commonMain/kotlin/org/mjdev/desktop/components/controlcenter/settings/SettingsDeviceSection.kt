package org.mjdev.desktop.components.controlcenter.settings

import androidx.compose.runtime.Composable
import org.mjdev.desktop.data.NetDevice

/** Shared status labels for network device sections. */
object NetDeviceLabels {
    const val UP = "Up"
    const val DOWN = "Down"
    const val NONE = "No devices found"
}

/** Lists [devices] as rows of "name : state (mac)" inside a titled section. */
@Suppress("FunctionName")
@Composable
fun SettingsDeviceSection(
    title: String,
    devices: Collection<NetDevice>,
) {
    SettingsSection(title = title) {
        if (devices.isEmpty()) {
            SettingsInfoRow(label = NetDeviceLabels.NONE, value = "")
        }
        devices.forEach { device ->
            val state = if (device.isUp) NetDeviceLabels.UP else NetDeviceLabels.DOWN
            SettingsInfoRow(label = device.name, value = "$state  ${device.macAddress}".trim())
        }
    }
}

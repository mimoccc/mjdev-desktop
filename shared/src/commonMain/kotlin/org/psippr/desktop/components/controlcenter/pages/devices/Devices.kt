package org.psippr.desktop.components.controlcenter.pages.devices

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import org.psippr.desktop.components.controlcenter.base.ControlCenterPage
import org.psippr.desktop.components.controlcenter.settings.SettingsDeviceSection
import org.psippr.desktop.components.controlcenter.settings.SettingsPageColumn
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.data.NetDevice
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.LaunchedEffect.flowBlock
import org.psippr.desktop.icons.mobile.MobileFriendly

/** Title of the connected devices section. */
private const val CONNECTED = "Connected devices"

/** Refresh period of the device list in milliseconds. */
private const val REFRESH_MS = 1000L

/** Control center page listing currently connected devices; hidden when there are none. */
@Suppress("FunctionName")
fun DevicesPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = Icons.Filled.Devices,
    name = "Connected devices",
    condition = {
        connectionManager.hasConnectedDevices
    },
) {
    val devices: Map<String, NetDevice> by flowBlock(
        emptyMap(),
        REFRESH_MS,
    ) { context.connectionManager.connectedDevices.toMap() }
    SettingsPageColumn {
        SettingsDeviceSection(CONNECTED, devices.values)
    }
}

@Preview
@Composable
fun DevicesPagePreview() = preview {
    DevicesPage(context).Render()
}

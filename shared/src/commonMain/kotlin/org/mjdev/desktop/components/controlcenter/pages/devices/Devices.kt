package org.mjdev.desktop.components.controlcenter.pages.devices

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.components.controlcenter.settings.SettingsDeviceSection
import org.mjdev.desktop.components.controlcenter.settings.SettingsPageColumn
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.data.NetDevice
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.LaunchedEffect.flowBlock
import org.mjdev.desktop.icons.mobile.MobileFriendly

/** Title of the connected devices section. */
private const val CONNECTED = "Connected devices"

/** Refresh period of the device list in milliseconds. */
private const val REFRESH_MS = 1000L

/** Control center page listing currently connected devices; hidden when there are none. */
@Suppress("FunctionName")
fun DevicesPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = MobileFriendly,
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

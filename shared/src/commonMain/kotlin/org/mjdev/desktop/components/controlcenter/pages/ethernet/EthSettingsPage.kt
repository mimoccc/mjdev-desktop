package org.mjdev.desktop.components.controlcenter.pages.ethernet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.components.controlcenter.settings.SettingsDeviceSection
import org.mjdev.desktop.components.controlcenter.settings.SettingsNetDeviceSection
import org.mjdev.desktop.components.controlcenter.settings.SettingsPageColumn
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.data.NetDevice
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.LaunchedEffect.flowBlock
import org.mjdev.desktop.icons.settings.SettingsEthernet

/** Title of the adapters section. */
private const val ADAPTERS = "Ethernet adapters"

/** Refresh period of the adapter list in milliseconds. */
private const val REFRESH_MS = 1000L

/** Control center page listing wired network adapters; hidden when none is present. */
@Suppress("FunctionName")
fun EthSettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = SettingsEthernet,
    name = "Ethernet",
    condition = {
        connectionManager.isEthAdapterAvailable
    },
) {
    val devices: Map<String, NetDevice> by flowBlock(
        emptyMap(),
        REFRESH_MS,
    ) { context.connectionManager.ethDevices.toMap() }
    SettingsPageColumn {
        if (devices.isEmpty()) {
            SettingsDeviceSection(ADAPTERS, devices.values)
        }
        devices.values.forEach { device ->
            SettingsNetDeviceSection(device, context.connectionManager)
        }
    }
}

@Preview
@Composable
fun EthSettingsPagePreview() = preview {
    EthSettingsPage(context).Render()
}

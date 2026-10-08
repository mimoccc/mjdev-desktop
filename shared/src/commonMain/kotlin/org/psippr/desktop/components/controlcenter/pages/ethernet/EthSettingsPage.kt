package org.psippr.desktop.components.controlcenter.pages.ethernet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import org.psippr.desktop.components.controlcenter.base.ControlCenterPage
import org.psippr.desktop.components.controlcenter.settings.SettingsDeviceSection
import org.psippr.desktop.components.controlcenter.settings.SettingsNetDeviceSection
import org.psippr.desktop.components.controlcenter.settings.SettingsPageColumn
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.data.NetDevice
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.LaunchedEffect.flowBlock
import org.psippr.desktop.icons.settings.SettingsEthernet

/** Title of the adapters section. */
private const val ADAPTERS = "Ethernet adapters"

/** Refresh period of the adapter list in milliseconds. */
private const val REFRESH_MS = 1000L

/** Control center page listing wired network adapters; hidden when none is present. */
@Suppress("FunctionName")
fun EthSettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = Icons.Filled.SettingsEthernet,
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

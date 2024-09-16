package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage

@Suppress("FunctionName")
fun EthSettingsPage() = ControlCenterPage(
    icon = Icons.Filled.SettingsEthernet,
    name = "Ethernet",
    condition = { api.connection.isEthAdapterAvailable }
) {
    Box(
        modifier = Modifier.fillMaxSize()
    )
}

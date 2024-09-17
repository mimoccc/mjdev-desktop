package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage

@Suppress("FunctionName")
fun WifiSettingsPage() = ControlCenterPage(
    icon = Icons.Filled.Wifi,
    name = "Wifi",
    condition = { connection.isWifiAdapterAvailable }
) {
    Box(
        modifier = Modifier.fillMaxSize()
    )
}

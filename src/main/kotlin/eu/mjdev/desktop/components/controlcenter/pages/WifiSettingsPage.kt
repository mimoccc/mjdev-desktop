package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.controlcenter.base.ControlCenterPage

@Suppress("FunctionName")
fun WifiSettingsPage() = ControlCenterPage(
    icon = Icons.Filled.Wifi,
    name = "Wifi",
    condition = { connectionManager.isWifiAdapterAvailable }
) {
    Box(
        modifier = Modifier.fillMaxSize()
    )
}

@Preview
@Composable
fun WifiSettingsPagePreview() = WifiSettingsPage().render()

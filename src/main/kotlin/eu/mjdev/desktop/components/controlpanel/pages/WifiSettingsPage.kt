package eu.mjdev.desktop.components.controlpanel.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.controlpanel.ControlCenterPage

@Suppress("FunctionName")
fun WifiSettingsPage() = ControlCenterPage(
    icon = Icons.Filled.Wifi,
    name = "Wifi"
) { backgroundColor ->
    Box(
        modifier = Modifier.fillMaxSize().background(backgroundColor)
    )
}

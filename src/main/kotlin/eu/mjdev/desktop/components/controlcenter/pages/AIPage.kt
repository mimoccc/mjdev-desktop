package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage

@Suppress("FunctionName")
fun AIPage() = ControlCenterPage(
    icon = Icons.Filled.Campaign,
    name = "Bluetooth"
) { backgroundColor ->
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}

package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage

@Suppress("FunctionName")
fun DevicesPage() = ControlCenterPage(
    icon = Icons.Filled.MobileFriendly,
    name = "Bluetooth"
) { backgroundColor ->
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}

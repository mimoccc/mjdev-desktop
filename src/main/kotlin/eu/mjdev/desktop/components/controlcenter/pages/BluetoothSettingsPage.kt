package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import eu.mjdev.desktop.extensions.Compose.preview

@Suppress("FunctionName")
fun BluetoothSettingsPage() = ControlCenterPage(
    icon = Icons.Filled.Bluetooth,
    name = "Bluetooth",
    condition = { connectionManager.isBthAdapterAvailable }
) {
    Box(
        modifier = Modifier.fillMaxSize()
    )
}

@Preview
@Composable
fun BluetoothSettingsPagePreview() = preview {
    BluetoothSettingsPage().render()
}

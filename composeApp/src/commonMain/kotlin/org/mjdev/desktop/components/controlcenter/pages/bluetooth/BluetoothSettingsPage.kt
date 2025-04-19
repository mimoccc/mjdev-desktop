package org.mjdev.desktop.components.controlcenter.pages.bluetooth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.network.Bluetooth
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("FunctionName")
fun BluetoothSettingsPage() = ControlCenterPage(
    icon = Bluetooth,
    name = "Bluetooth",
    condition = {
        false
//        connectionManager.isBthAdapterAvailable
    }
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

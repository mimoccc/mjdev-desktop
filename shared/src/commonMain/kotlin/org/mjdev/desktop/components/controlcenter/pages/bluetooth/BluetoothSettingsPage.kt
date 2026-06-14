package org.mjdev.desktop.components.controlcenter.pages.bluetooth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.network.Bluetooth

@Suppress("FunctionName")
fun BluetoothSettingsPage(context: IDesktopContext) =
    ControlCenterPage(
        context = context,
        icon = Bluetooth,
        name = "Bluetooth",
        condition = {
            false
//        connectionManager.isBthAdapterAvailable
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        )
    }

@Preview
@Composable
fun BluetoothSettingsPagePreview() =
    preview {
        BluetoothSettingsPage(context).Render()
    }

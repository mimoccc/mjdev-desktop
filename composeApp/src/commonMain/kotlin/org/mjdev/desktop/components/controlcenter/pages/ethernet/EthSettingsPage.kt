package org.mjdev.desktop.components.controlcenter.pages.ethernet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.settings.SettingsEthernet
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("FunctionName")
fun EthSettingsPage() = ControlCenterPage(
    icon = SettingsEthernet,
    name = "Ethernet",
    condition = {
        false
//        connectionManager.isEthAdapterAvailable
    }
) {
    Box(
        modifier = Modifier.fillMaxSize()
    )
}

@Preview
@Composable
fun EthSettingsPagePreview() = preview {
    EthSettingsPage().Render()
}

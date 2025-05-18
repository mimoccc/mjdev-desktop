package org.mjdev.desktop.components.controlcenter.pages.display

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.settings.SettingsMonitor
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("FunctionName")
fun DisplaySettingsPage() = ControlCenterPage(
    icon = SettingsMonitor,
    name = "Display"
) {
    Box(
        modifier = Modifier.fillMaxSize()
    )
}

@Preview
@Composable
fun DisplaySettingsPagePreview() = preview {
    DisplaySettingsPage().Render()
}

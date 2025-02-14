package org.mjdev.desktop.components.controlcenter.pages.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.custom.Mjdev

@Suppress("FunctionName")
fun ThemeSettingsPage() = ControlCenterPage(
    icon = Mjdev, // todo
    name = "Theme",
    condition = { true }
) {
    Box(
        modifier = Modifier.fillMaxSize()
    )
}

@Suppress("unused")
//@Preview
@Composable
fun ThemeSettingsPagePreview() = preview {
    ThemeSettingsPage().render()
}

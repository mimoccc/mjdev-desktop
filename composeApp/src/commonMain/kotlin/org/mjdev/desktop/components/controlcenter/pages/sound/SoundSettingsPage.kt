package org.mjdev.desktop.components.controlcenter.pages.sound

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.settings.SettingsSound

@Suppress("FunctionName")
fun SoundSettingsPage() = ControlCenterPage(
    icon = SettingsSound,
    name = "Sound",
    condition = { true } // todo sound manager
) {
    Box(
        modifier = Modifier.fillMaxSize()
    )
}

@Suppress("unused")
//@Preview
@Composable
fun SoundSettingsPagePreview() = preview {
    SoundSettingsPage().render()
}

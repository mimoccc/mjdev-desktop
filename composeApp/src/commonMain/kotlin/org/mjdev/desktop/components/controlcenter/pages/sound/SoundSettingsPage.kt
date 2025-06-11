package org.mjdev.desktop.components.controlcenter.pages.sound

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.settings.SettingsSound
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.context.IDesktopContext

@Suppress("FunctionName")
fun SoundSettingsPage(
    context: IDesktopContext
) = ControlCenterPage(
    context = context,
    icon = SettingsSound,
    name = "Sound",
    condition = { true } // todo sound manager
) {
    Box(
        modifier = Modifier.fillMaxSize()
    )
}

@Preview
@Composable
fun PreviewSoundSettingsPage() = preview {
    SoundSettingsPage(context).Render()
}

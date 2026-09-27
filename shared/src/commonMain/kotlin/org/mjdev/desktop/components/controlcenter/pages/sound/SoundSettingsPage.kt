package org.mjdev.desktop.components.controlcenter.pages.sound

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.settings.SettingsSound

// Original placeholder implementation, kept for history:
// fun SoundSettingsPage(context: IDesktopContext) = ControlCenterPage(
//     context = context,
//     icon = SettingsSound,
//     name = "Sound",
//     condition = { true }, // todo sound manager
// ) {
//     Box(
//         modifier = Modifier.fillMaxSize(),
//     )
// }

/**
 * Control center page allowing the user to control the system output volume
 * and toggle mute, backed by [IDesktopContext.volumeManager].
 */
@Suppress("FunctionName")
fun SoundSettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = SettingsSound,
    name = "Sound",
    condition = { true },
) {
    var volume by remember { mutableStateOf(context.volumeManager.volume) }
    var muted by remember { mutableStateOf(context.volumeManager.isMuted) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SoundSettingsPageDefaults.contentPadding),
        verticalArrangement = Arrangement.spacedBy(SoundSettingsPageDefaults.itemSpacing),
    ) {
        Text(text = SoundSettingsPageDefaults.volumeLabel)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    muted = context.volumeManager.toggleMute()
                },
            ) {
                Icon(
                    imageVector = if (muted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    contentDescription = SoundSettingsPageDefaults.muteToggleDescription,
                )
            }
            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = volume,
                onValueChange = { newValue ->
                    volume = newValue
                    context.volumeManager.setVolume(newValue)
                },
                valueRange = SoundSettingsPageDefaults.volumeRange,
            )
        }
    }
}

@Preview
@Composable
fun PreviewSoundSettingsPage() = preview {
    SoundSettingsPage(context).Render()
}

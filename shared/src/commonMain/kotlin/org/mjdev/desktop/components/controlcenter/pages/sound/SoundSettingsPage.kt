package org.mjdev.desktop.components.controlcenter.pages.sound

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.components.controlcenter.settings.SettingsPageColumn
import org.mjdev.desktop.components.controlcenter.settings.SettingsSection
import org.mjdev.desktop.components.controlcenter.settings.SettingsSelectRow
import org.mjdev.desktop.components.controlcenter.settings.SettingsSliderRow
import org.mjdev.desktop.components.controlcenter.settings.SettingsSwitchRow
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
 * and toggle mute, backed by [IDesktopContext.volumeManager]. Colored from the wallpaper palette.
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
    var outputs by remember { mutableStateOf(context.volumeManager.outputs) }
    SettingsPageColumn {
        if (outputs.isNotEmpty()) {
            SettingsSection(title = SoundSettingsPageDefaults.deviceTitle) {
                SettingsSelectRow(
                    label = SoundSettingsPageDefaults.deviceLabel,
                    selected = outputs.firstOrNull { it.isDefault }?.name.orEmpty(),
                    options = outputs.map { it.name },
                    onSelected = { name ->
                        context.volumeManager.setDefaultOutput(name)
                        // Re-read so the picker shows what the system really selected.
                        outputs = context.volumeManager.outputs
                        volume = context.volumeManager.volume
                        muted = context.volumeManager.isMuted
                    },
                )
            }
        }
        SettingsSection(title = SoundSettingsPageDefaults.outputTitle) {
            SettingsSliderRow(
                label = SoundSettingsPageDefaults.volumeLabel,
                value = volume,
                valueRange = SoundSettingsPageDefaults.volumeRange,
                format = { value -> "${(value * SoundSettingsPageDefaults.PERCENT).toInt()}%" },
                onValueChange = { newValue ->
                    volume = newValue
                    context.volumeManager.setVolume(newValue)
                },
            )
            SettingsSwitchRow(
                label = SoundSettingsPageDefaults.muteLabel,
                checked = muted,
                onCheckedChange = {
                    muted = context.volumeManager.toggleMute()
                },
            )
        }
    }
}

@Preview
@Composable
fun PreviewSoundSettingsPage() = preview {
    SoundSettingsPage(context).Render()
}

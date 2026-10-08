package org.psippr.desktop.components.controlcenter.pages.sound

import androidx.compose.runtime.Composable
import org.psippr.desktop.components.controlcenter.settings.SettingsChoice
import org.psippr.desktop.components.controlcenter.settings.SettingsChoiceList
import org.psippr.desktop.components.controlcenter.settings.SettingsInfoRow
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.components.controlcenter.settings.SettingsSliderRow
import org.psippr.desktop.components.controlcenter.settings.SettingsSwitchRow
import org.psippr.desktop.managers.volume.AudioDevice

/**
 * One Sound settings section laid out like Ubuntu Settings: device list, volume, mute,
 * plus optional extra content (the output section adds the card configuration).
 */
@Suppress("FunctionName")
@Composable
fun SoundDeviceSection(
    title: String,
    deviceLabel: String,
    volumeLabel: String,
    state: SoundDeviceState,
    onSelect: (AudioDevice) -> Unit,
    onVolume: (Float) -> Unit,
    onToggleMute: () -> Unit,
    extra: @Composable () -> Unit = {},
) = SettingsSection(title = title) {
    if (state.devices.isEmpty()) {
        SettingsInfoRow(label = deviceLabel, value = SoundSettingsPageDefaults.noDevices)
    } else {
        SettingsChoiceList(
            label = deviceLabel,
            options = state.devices.map { device -> SettingsChoice(device.key, device.label, device.isDefault) },
            onSelected = { choice -> state.devices.firstOrNull { device -> device.key == choice.key }?.let(onSelect) },
        )
    }
    extra()
    SettingsSliderRow(
        label = volumeLabel,
        value = state.volume,
        valueRange = SoundSettingsPageDefaults.volumeRange,
        format = { value -> "${(value * SoundSettingsPageDefaults.PERCENT).toInt()}%" },
        onValueChange = onVolume,
    )
    SettingsSwitchRow(
        label = SoundSettingsPageDefaults.muteLabel,
        checked = state.muted,
        onCheckedChange = { onToggleMute() },
    )
}

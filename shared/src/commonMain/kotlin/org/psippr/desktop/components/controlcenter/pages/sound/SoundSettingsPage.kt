package org.psippr.desktop.components.controlcenter.pages.sound

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.psippr.desktop.components.controlcenter.base.ControlCenterPage
import org.psippr.desktop.components.controlcenter.settings.SettingsChoice
import org.psippr.desktop.components.controlcenter.settings.SettingsChoiceList
import org.psippr.desktop.components.controlcenter.settings.SettingsPageColumn
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.components.controlcenter.settings.SettingsSelectRow
import org.psippr.desktop.components.controlcenter.settings.SettingsSliderRow
import org.psippr.desktop.components.controlcenter.settings.SettingsSwitchRow
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.icons.settings.SettingsSound
import org.psippr.desktop.managers.volume.AudioDeviceKind
import org.psippr.desktop.managers.volume.AudioProfile

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

// Previous version (output only, raw sink names in a popup that could not be used, no input):
// /**
//  * Control center page allowing the user to control the system output volume
//  * and toggle mute, backed by [IDesktopContext.volumeManager]. Colored from the wallpaper palette.
//  */
// @Suppress("FunctionName")
// fun SoundSettingsPage(context: IDesktopContext) = ControlCenterPage(
//     context = context,
//     icon = Icons.Filled.VolumeUp,
//     name = "Sound",
//     condition = { true },
// ) {
//     var volume by remember { mutableStateOf(context.volumeManager.volume) }
//     var muted by remember { mutableStateOf(context.volumeManager.isMuted) }
//     var outputs by remember { mutableStateOf(context.volumeManager.outputs) }
//     SettingsPageColumn {
//         if (outputs.isNotEmpty()) {
//             SettingsSection(title = SoundSettingsPageDefaults.deviceTitle) {
//                 SettingsSelectRow(
//                     label = SoundSettingsPageDefaults.deviceLabel,
//                     selected = outputs.firstOrNull { it.isDefault }?.name.orEmpty(),
//                     options = outputs.map { it.name },
//                     onSelected = { name ->
//                         context.volumeManager.setDefaultOutput(name)
//                         // Re-read so the picker shows what the system really selected.
//                         outputs = context.volumeManager.outputs
//                         volume = context.volumeManager.volume
//                         muted = context.volumeManager.isMuted
//                     },
//                 )
//             }
//         }
//         SettingsSection(title = SoundSettingsPageDefaults.outputTitle) {
//             SettingsSliderRow(
//                 label = SoundSettingsPageDefaults.volumeLabel,
//                 value = volume,
//                 valueRange = SoundSettingsPageDefaults.volumeRange,
//                 format = { value -> "${(value * SoundSettingsPageDefaults.PERCENT).toInt()}%" },
//                 onValueChange = { newValue ->
//                     volume = newValue
//                     context.volumeManager.setVolume(newValue)
//                 },
//             )
//             SettingsSwitchRow(
//                 label = SoundSettingsPageDefaults.muteLabel,
//                 checked = muted,
//                 onCheckedChange = {
//                     muted = context.volumeManager.toggleMute()
//                 },
//             )
//         }
//     }
// }

/**
 * Control center Sound page laid out like Ubuntu Settings Sound: Output (device, configuration,
 * volume, mute) and Input (device, volume, mute), backed by [IDesktopContext.volumeManager].
 * Devices are re-read only when the sound system reports a change ([IVolumeManager.watchChanges],
 * no polling); all system calls run off the UI thread. Colored from the wallpaper palette.
 */
@Suppress("FunctionName")
fun SoundSettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = Icons.Filled.VolumeUp,
    name = "Sound",
    condition = { true },
) {
    val manager = context.volumeManager
    val output = remember { SoundDeviceState(manager, AudioDeviceKind.OUTPUT) }
    val input = remember { SoundDeviceState(manager, AudioDeviceKind.INPUT) }
    var profiles by remember { mutableStateOf(emptyList<AudioProfile>()) }
    val scope = rememberCoroutineScope()
    // runs a system call off the UI thread, then refreshes the configuration list
    val background: (() -> Unit) -> Unit = { action ->
        scope.launch(Dispatchers.IO) {
            action()
            profiles = manager.profiles
        }
    }
    // old: re-read everything every REFRESH_MS (polling, wasted resources)
    // LaunchedEffect(Unit) {
    //     while (isActive) {
    //         withContext(Dispatchers.IO) {
    //             output.reload()
    //             input.reload()
    //             profiles = manager.profiles
    //         }
    //         delay(SoundSettingsPageDefaults.REFRESH_MS)
    //     }
    // }
    // event driven: load once, then only when the system reports a change; a burst of events
    // collapses into one reload (conflated channel); the watch ends when the page closes
    LaunchedEffect(Unit) {
        val changes = Channel<Unit>(Channel.CONFLATED)
        changes.trySend(Unit)
        val watch = manager.watchChanges { changes.trySend(Unit) }
        try {
            for (change in changes) {
                withContext(Dispatchers.IO) {
                    output.reload()
                    input.reload()
                    profiles = manager.profiles
                }
            }
        } finally {
            watch.close()
        }
    }
    SettingsPageColumn {
        SoundDeviceSection(
            title = SoundSettingsPageDefaults.outputTitle,
            deviceLabel = SoundSettingsPageDefaults.outputDeviceLabel,
            volumeLabel = SoundSettingsPageDefaults.outputVolumeLabel,
            state = output,
            onSelect = { device -> background { output.select(device) } },
            onVolume = { value -> background { output.changeVolume(value) } },
            onToggleMute = { background { output.toggleMute() } },
        ) {
            if (profiles.size > 1) {
                SettingsChoiceList(
                    label = SoundSettingsPageDefaults.profileLabel,
                    options = profiles.map { profile -> SettingsChoice(profile.id, profile.label, profile.isActive) },
                    onSelected = { choice ->
                        profiles.firstOrNull { profile -> profile.id == choice.key }?.let { profile ->
                            background {
                                manager.setProfile(profile)
                                output.reload()
                                input.reload()
                            }
                        }
                    },
                )
            }
        }
        SoundDeviceSection(
            title = SoundSettingsPageDefaults.inputTitle,
            deviceLabel = SoundSettingsPageDefaults.inputDeviceLabel,
            volumeLabel = SoundSettingsPageDefaults.inputVolumeLabel,
            state = input,
            onSelect = { device -> background { input.select(device) } },
            onVolume = { value -> background { input.changeVolume(value) } },
            onToggleMute = { background { input.toggleMute() } },
        )
    }
}

@Preview
@Composable
fun PreviewSoundSettingsPage() = preview {
    SoundSettingsPage(context).Render()
}

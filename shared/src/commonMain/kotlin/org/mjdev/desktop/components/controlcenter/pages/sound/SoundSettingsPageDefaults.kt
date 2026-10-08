package org.mjdev.desktop.components.controlcenter.pages.sound

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Constants used by [SoundSettingsPage], avoiding magic numbers and hardcoded strings.
 */
object SoundSettingsPageDefaults {
    /** Padding applied around the sound settings page content. */
    val contentPadding: Dp = 16.dp

    /** Vertical spacing between items in the sound settings page. */
    val itemSpacing: Dp = 12.dp

    /** Valid range for the volume slider. */
    val volumeRange: ClosedFloatingPointRange<Float> = 0f..1f

    /** Label displayed above the volume slider. */
    const val volumeLabel: String = "Volume"

    /** Title of the output settings section. */
    const val outputTitle: String = "Output"

    /** Title of the output device section. */
    const val deviceTitle: String = "Output device"

    /** Label of the output device picker. */
    const val deviceLabel: String = "Play sound through"

    /** Label of the mute switch. */
    const val muteLabel: String = "Muted"

    /** Multiplier converting the 0..1 volume into a percentage. */
    const val PERCENT: Int = 100

    /** Accessibility description for the mute toggle button. */
    const val muteToggleDescription: String = "Toggle mute"

    /** Title of the input section (Ubuntu Settings wording). */
    const val inputTitle: String = "Input"

    /** Label of the output device list. */
    const val outputDeviceLabel: String = "Output Device"

    /** Label of the input device list. */
    const val inputDeviceLabel: String = "Input Device"

    /** Label of the output volume slider. */
    const val outputVolumeLabel: String = "Output Volume"

    /** Label of the input volume slider. */
    const val inputVolumeLabel: String = "Input Volume"

    /** Label of the sound card configuration (profile) list. */
    const val profileLabel: String = "Configuration"

    /** Shown when the system reports no device of a kind. */
    const val noDevices: String = "No devices found"

    /** Old polling interval (ms), no longer used: devices are re-read on system change events. */
    const val REFRESH_MS: Long = 2000L
}

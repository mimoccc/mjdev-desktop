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

    /** Accessibility description for the mute toggle button. */
    const val muteToggleDescription: String = "Toggle mute"
}

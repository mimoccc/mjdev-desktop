package org.mjdev.desktop.components.controlcenter.pages.sound

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.mjdev.desktop.managers.volume.AudioDevice
import org.mjdev.desktop.managers.volume.AudioDeviceKind
import org.mjdev.desktop.managers.volume.IVolumeManager

/**
 * UI state of one Sound settings section (Output or Input): devices, volume and mute of the default
 * device. Calls into [IVolumeManager] block on a process, so call them from a background dispatcher.
 */
@Stable
class SoundDeviceState(
    private val manager: IVolumeManager,
    /** Section direction. */
    val kind: AudioDeviceKind,
) {
    /** Selectable devices, the default one marked. */
    var devices: List<AudioDevice> by mutableStateOf(emptyList())
        private set

    /** Volume of the default device, 0f..1f. */
    var volume: Float by mutableStateOf(0f)
        private set

    /** Mute state of the default device. */
    var muted: Boolean by mutableStateOf(false)
        private set

    /** Re-reads everything from the system. */
    fun reload() {
        devices = manager.devices(kind)
        volume = manager.volume(kind)
        muted = manager.isMuted(kind)
    }

    /** Makes [device] the default and re-reads (volume / mute belong to the new device). */
    fun select(device: AudioDevice) {
        manager.setDefaultDevice(device)
        reload()
    }

    /** Shows [value] at once, then applies it. */
    fun changeVolume(value: Float) {
        volume = value
        manager.setVolume(kind, value)
    }

    /** Toggles mute and shows the real new state. */
    fun toggleMute() {
        muted = manager.toggleMute(kind)
    }
}

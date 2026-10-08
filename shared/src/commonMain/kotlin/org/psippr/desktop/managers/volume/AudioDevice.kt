package org.psippr.desktop.managers.volume

/**
 * One selectable audio device as shown in the Sound settings, like the Ubuntu Settings
 * "Output Device" / "Input Device" lists: a system device, or one port of it (Speakers, Headphones…).
 */
data class AudioDevice(
    /** Output or input. */
    val kind: AudioDeviceKind,
    /** System id of the device (PulseAudio / PipeWire sink or source name). */
    val id: String,
    /** Port of the device to activate, null when the device has no ports. */
    val port: String? = null,
    /** Human readable name shown to the user. */
    val label: String = id,
    /** True when this is the current default device (and active port). */
    val isDefault: Boolean = false,
) {
    /** Unique key of this choice (device + port). */
    val key: String
        get() = port?.let { p -> "$id$KEY_SEPARATOR$p" } ?: id

    companion object {
        /** Separator between device id and port in [key]. */
        const val KEY_SEPARATOR = "#"
    }
}

package org.mjdev.desktop.managers.volume

/** One audio output device (sink) the system can play sound through. */
data class AudioOutput(
    /** System name of the sink, used to select it. */
    val name: String = "",
    /** True when this sink is the current default output. */
    val isDefault: Boolean = false,
)

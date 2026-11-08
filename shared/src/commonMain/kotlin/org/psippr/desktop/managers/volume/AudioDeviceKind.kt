package org.psippr.desktop.managers.volume

/** Direction of an audio device, matching the Output / Input sections of the Sound settings. */
enum class AudioDeviceKind {
    /** Playback device (speakers, headphones, HDMI). */
    OUTPUT,

    /** Capture device (microphones). */
    INPUT,
}

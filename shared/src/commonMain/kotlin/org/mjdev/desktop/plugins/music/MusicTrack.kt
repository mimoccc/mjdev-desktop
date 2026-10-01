package org.mjdev.desktop.plugins.music

/** One playable file found in the music folder. */
data class MusicTrack(
    /** Stable id of the track, its absolute path. */
    val id: String,
    /** Label shown in the widget, the file name without extension. */
    val title: String,
    /** Absolute path handed to the audio player. */
    val path: String,
)

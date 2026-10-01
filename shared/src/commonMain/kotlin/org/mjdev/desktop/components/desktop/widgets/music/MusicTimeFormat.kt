package org.mjdev.desktop.components.desktop.widgets.music

import org.mjdev.desktop.plugins.music.MusicDefaults

/** Formats a position in milliseconds as `m:ss`; negative values count as zero. */
fun formatMusicTime(millis: Long): String {
    val totalSeconds = millis.coerceAtLeast(0L) / MusicDefaults.MILLIS_IN_SECOND
    val minutes = totalSeconds / MusicDefaults.SECONDS_IN_MINUTE
    val seconds = totalSeconds % MusicDefaults.SECONDS_IN_MINUTE
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

package org.psippr.desktop.plugins.music

/**
 * Name of the folder a track lies in, shown under the title. Music folders are usually named by
 * artist or album, so it stands in for the artist line (tracks carry no tags).
 */
object MusicTrackFolder {
    /** Path separator of the track paths. */
    private const val SEPARATOR = '/'

    /** Windows path separator, normalized to [SEPARATOR]. */
    private const val WINDOWS_SEPARATOR = '\\'

    /** Folder name of [track], or empty when there is none. */
    fun of(track: MusicTrack?): String = track
        ?.path
        ?.replace(WINDOWS_SEPARATOR, SEPARATOR)
        ?.substringBeforeLast(SEPARATOR, missingDelimiterValue = "")
        ?.substringAfterLast(SEPARATOR)
        .orEmpty()
}

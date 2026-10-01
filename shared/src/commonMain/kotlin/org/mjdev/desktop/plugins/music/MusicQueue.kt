package org.mjdev.desktop.plugins.music

/**
 * Immutable play queue over [tracks] with a cursor at [index]. Moving past either end wraps
 * around, so next/previous always land on a track while the queue is not empty.
 */
data class MusicQueue(
    /** Tracks in play order. */
    val tracks: List<MusicTrack> = emptyList(),
    /** Position of the current track in [tracks]. */
    val index: Int = 0,
) {
    /** The track the cursor points at, or null for an empty queue. */
    val current: MusicTrack?
        get() = tracks.getOrNull(index)

    /** Queue moved to the following track, wrapping to the first one after the last. */
    fun next(): MusicQueue = moveBy(1)

    /** Queue moved to the preceding track, wrapping to the last one before the first. */
    fun previous(): MusicQueue = moveBy(-1)

    private fun moveBy(step: Int): MusicQueue =
        if (tracks.isEmpty()) this else copy(index = (index + step).mod(tracks.size))
}

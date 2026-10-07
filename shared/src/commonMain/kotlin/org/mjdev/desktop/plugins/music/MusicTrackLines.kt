package org.mjdev.desktop.plugins.music

/**
 * The three track titles of the inner card, shown like lyric lines: the current track in the
 * middle, the previous and next ones faded around it. Empty when there is no other track.
 *
 * @property previous title of the track before the current one
 * @property current title of the current track
 * @property next title of the track after the current one
 */
data class MusicTrackLines(
    val previous: String,
    val current: String,
    val next: String,
) {
    companion object {
        /** Lines of [queue]; a queue of one track shows no neighbours (they would repeat it). */
        fun of(queue: MusicQueue): MusicTrackLines {
            val hasNeighbours = queue.tracks.size > 1
            return MusicTrackLines(
                previous = if (hasNeighbours) queue.previous().title() else MusicDefaults.NO_TRACK_LINE,
                current = queue.current?.title ?: MusicDefaults.EMPTY_TITLE,
                next = if (hasNeighbours) queue.next().title() else MusicDefaults.NO_TRACK_LINE,
            )
        }

        // title of the current track of this queue, empty when none
        private fun MusicQueue.title(): String = current?.title.orEmpty()
    }
}

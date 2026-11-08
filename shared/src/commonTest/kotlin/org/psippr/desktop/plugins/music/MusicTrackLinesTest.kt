package org.psippr.desktop.plugins.music

import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression tests of the music card texts: neighbour lines and the folder line. */
class MusicTrackLinesTest {
    private val tracks =
        listOf("a", "b", "c").map { name ->
            MusicTrack(id = name, title = name, path = "/music/Artist/$name.mp3")
        }

    @Test
    fun showsPreviousCurrentAndNextWithWrapAround() {
        assertEquals(MusicTrackLines("c", "a", "b"), MusicTrackLines.of(MusicQueue(tracks, index = 0)))
    }

    @Test
    fun singleTrackHasNoRepeatedNeighbours() {
        val lines = MusicTrackLines.of(MusicQueue(tracks.take(1)))
        assertEquals(MusicTrackLines(MusicDefaults.NO_TRACK_LINE, "a", MusicDefaults.NO_TRACK_LINE), lines)
    }

    @Test
    fun emptyQueueShowsEmptyTitle() {
        assertEquals(MusicDefaults.EMPTY_TITLE, MusicTrackLines.of(MusicQueue()).current)
    }

    @Test
    fun folderLineIsTheParentFolderName() {
        assertEquals("Artist", MusicTrackFolder.of(tracks.first()))
        assertEquals("Album", MusicTrackFolder.of(MusicTrack(id = "w", title = "w", path = "C:\\Music\\Album\\w.mp3")))
        assertEquals("", MusicTrackFolder.of(MusicTrack(id = "x", title = "x", path = "x.mp3")))
        assertEquals("", MusicTrackFolder.of(null))
    }
}

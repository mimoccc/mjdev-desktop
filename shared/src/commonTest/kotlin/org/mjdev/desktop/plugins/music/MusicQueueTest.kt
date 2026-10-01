package org.mjdev.desktop.plugins.music

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Regression tests of the queue cursor: wrap-around and the empty queue must never crash. */
class MusicQueueTest {
    private val tracks = listOf("a", "b", "c").map { name -> MusicTrack(id = name, title = name, path = "/m/$name") }

    @Test
    fun nextWrapsFromLastToFirst() {
        assertEquals("a", MusicQueue(tracks, index = 2).next().current?.id)
    }

    @Test
    fun previousWrapsFromFirstToLast() {
        assertEquals("c", MusicQueue(tracks, index = 0).previous().current?.id)
    }

    @Test
    fun emptyQueueHasNoCurrentAndStaysPut() {
        val empty = MusicQueue()
        assertNull(empty.current)
        assertEquals(empty, empty.next())
        assertEquals(empty, empty.previous())
    }
}

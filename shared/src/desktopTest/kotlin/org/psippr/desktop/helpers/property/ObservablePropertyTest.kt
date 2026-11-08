package org.psippr.desktop.helpers.property

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression tests of the listenable property used for the wallpaper list. */
class ObservablePropertyTest {
    @Test
    fun lateListenerGetsLatestValue() {
        val property = ObservableProperty(listOf(FIRST))
        property.value = listOf(SECOND)
        assertEquals(listOf(SECOND), property.changes.value)
    }

    @Test
    fun equalValueIsNotResentSoListenersCannotLoop() {
        val property = ObservableProperty(listOf(FIRST))
        val seen = mutableListOf<List<String>>()
        runBlocking {
            val listener = launch(start = CoroutineStart.UNDISPATCHED) { property.changes.collect { seen += it } }
            property.value = listOf(FIRST)
            yield()
            property.value = listOf(SECOND)
            yield()
            listener.cancel()
        }
        assertEquals(listOf(listOf(FIRST), listOf(SECOND)), seen)
    }

    @Test
    fun delegateReadsAndWritesTheSameValue() {
        val property = ObservableProperty(0)
        var delegated by property
        delegated = UPDATES
        assertEquals(UPDATES, property.value)
    }

    @Test
    fun concurrentUpdatesAreNotLost() {
        val property = ObservableProperty(0)
        runBlocking {
            List(UPDATES) { launch(Dispatchers.Default) { property.update { count -> count + 1 } } }.joinAll()
        }
        assertEquals(UPDATES, property.value)
    }

    private companion object {
        /** Sample values. */
        const val FIRST = "first"
        const val SECOND = "second"

        /** Number of parallel updates in the concurrency test. */
        const val UPDATES = 1000
    }
}

package org.mjdev.desktop.data

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

/**
 * Regression tests of the "wallpaper occasionally does not work" bug: late loaded wallpapers
 * must reach listeners, a reload must never publish a cleared list, and users must not share
 * one config (loading another user used to wipe the wallpapers).
 */
class DesktopConfigBackgroundsTest {
    @Test
    fun wallpapersLoadedAfterListeningStartedReachTheListener() {
        val config = DesktopConfig()
        val seen = mutableListOf<List<Any>>()
        runBlocking {
            val listener =
                launch(start = CoroutineStart.UNDISPATCHED) {
                    config.backgroundsProperty.changes.collect { seen += it }
                }
            config.reloadBackgrounds(listOf(TestImagesProvider(listOf(FIRST)))).join()
            yield()
            listener.cancel()
        }
        assertEquals(listOf(emptyList(), listOf<Any>(FIRST)), seen)
    }

    @Test
    fun reloadPublishesAllProvidersAtOnceWithoutClearedList() {
        val config = DesktopConfig()
        runBlocking { config.reloadBackgrounds(listOf(TestImagesProvider(listOf(FIRST)))).join() }
        val seen = mutableListOf<List<Any>>()
        runBlocking {
            val listener =
                launch(start = CoroutineStart.UNDISPATCHED) {
                    config.backgroundsProperty.changes.collect { seen += it }
                }
            config
                .reloadBackgrounds(
                    listOf(
                        TestImagesProvider(listOf(FIRST)),
                        TestImagesProvider(listOf(SECOND)),
                    ),
                ).join()
            yield()
            listener.cancel()
        }
        assertEquals(listOf(listOf<Any>(FIRST), listOf(FIRST, SECOND)), seen)
    }

    @Test
    fun failingProviderDoesNotDropOtherWallpapers() {
        val config = DesktopConfig()
        runBlocking {
            config
                .reloadBackgrounds(
                    listOf(
                        TestImagesProvider(failure = IllegalStateException(FAILURE)),
                        TestImagesProvider(listOf(SECOND)),
                    ),
                ).join()
        }
        assertEquals(listOf<Any>(SECOND), config.backgrounds)
        assertEquals(listOf<Any>(SECOND), config.desktopBackgrounds)
    }

    @Test
    fun everyUserGetsOwnConfig() {
        val first = DesktopConfig.load(TestUser(FIRST_USER))
        val second = DesktopConfig.load(TestUser(SECOND_USER))
        assertNotSame(first, second)
        assertNotSame(first.backgroundsProperty, second.backgroundsProperty)
    }

    private companion object {
        /** Sample wallpapers. */
        const val FIRST = "/wallpapers/first.jpg"
        const val SECOND = "/wallpapers/second.jpg"

        /** Message of the simulated provider failure. */
        const val FAILURE = "provider failed"

        /** Unique test user names (the config cache is global). */
        const val FIRST_USER = "mjdev-test-user-first"
        const val SECOND_USER = "mjdev-test-user-second"
    }
}

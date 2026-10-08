package org.mjdev.desktop.helpers.image

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.use
import org.mjdev.desktop.helpers.image.ImageLoader.asyncImageLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression test: the image loader must survive recompositions. A new loader (with a new empty
 * cache) on every recomposition made the Control Center user picture blink while colors animated.
 */
class AsyncImageLoaderTest {
    @Test
    fun loaderIsTheSameInstanceAcrossRecompositions() {
        val tick = mutableIntStateOf(0)
        val loaders = mutableListOf<coil3.ImageLoader>()
        ImageComposeScene(width = SIZE, height = SIZE) {
            tick.intValue
            loaders.add(asyncImageLoader())
        }.use { scene ->
            repeat(RECOMPOSITIONS) { frame ->
                tick.intValue = frame + 1
                scene.render(frame * FRAME_NANOS)
            }
        }
        assertTrue(loaders.size > 1, "content was recomposed")
        assertEquals(1, loaders.distinct().size)
    }

    private companion object {
        const val SIZE = 10
        const val RECOMPOSITIONS = 5
        const val FRAME_NANOS = 16_000_000L
    }
}

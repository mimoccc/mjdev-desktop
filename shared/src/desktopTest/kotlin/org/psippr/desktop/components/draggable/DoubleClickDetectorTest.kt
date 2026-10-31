package org.psippr.desktop.components.draggable

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Regression: a desktop icon double click often did nothing, because the timer based detector
 * timed out while the busy UI thread handled the second click late; event times decide now.
 */
class DoubleClickDetectorTest {
    private val at = Offset(10f, 10f)

    private fun detector() = DoubleClickDetector(timeoutMillis = 400, slop = 8f)

    private fun DoubleClickDetector.click(
        time: Long,
        position: Offset = at,
    ): Boolean {
        onPress(position)
        return onRelease(time, position)
    }

    @Test
    fun twoQuickClicksAreADoubleClick() {
        val d = detector()
        assertFalse(d.click(1_000))
        assertTrue(d.click(1_200))
    }

    @Test
    fun slowSecondClickIsSingle() {
        val d = detector()
        d.click(1_000)
        assertFalse(d.click(1_500))
    }

    @Test
    fun dragIsNoClick() {
        val d = detector()
        d.click(1_000)
        d.onPress(at)
        assertFalse(d.onRelease(1_100, at + Offset(50f, 0f)))
    }

    @Test
    fun thirdClickStartsOver() {
        val d = detector()
        d.click(1_000)
        assertTrue(d.click(1_100))
        assertFalse(d.click(1_200))
    }

    @Test
    fun releaseWithoutPressIsIgnored() {
        assertFalse(detector().onRelease(1_000, at))
    }
}

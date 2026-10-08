package org.psippr.desktop.helpers.font.guard

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression tests: a child that ignores SIGTERM (hung shutdown) must still be stopped. */
class ChildProcessStopperTest {
    @Test
    fun childIgnoringSigtermIsKilled() {
        val child = start("trap '' TERM; sleep $CHILD_SLEEP_S")
        assertTrue(ChildProcessStopper.stop(child, GRACE_MS), "had to kill forcibly")
        assertFalse(child.isAlive)
    }

    @Test
    fun wellBehavedChildStopsWithoutKill() {
        val child = start("sleep $CHILD_SLEEP_S")
        assertFalse(ChildProcessStopper.stop(child, GRACE_MS), "SIGTERM was enough")
        assertFalse(child.isAlive)
    }

    // shell child; give it a moment so the trap is installed before the stop
    private fun start(script: String): Process = ProcessBuilder("sh", "-c", script).start().also {
        Thread.sleep(STARTUP_MS)
    }

    private companion object {
        const val CHILD_SLEEP_S = 60
        const val GRACE_MS = 500L
        const val STARTUP_MS = 300L
    }
}

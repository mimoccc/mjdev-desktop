package org.psippr.desktop.helpers.application

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression: a desktop whose shutdown hangs (UI thread stuck holding the AWT lock) could not be
 * stopped at all. A child JVM with a shutdown hook that never ends must still exit.
 */
class ShutdownWatchdogTest {
    @Test
    fun stuckShutdownIsHalted() {
        val java = File(System.getProperty("java.home"), "bin/java").path
        val process =
            ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), StuckShutdownMain::class.java.name)
                .redirectErrorStream(true)
                .start()
        val exited = process.waitFor(WAIT_S, TimeUnit.SECONDS)
        if (!exited) process.destroyForcibly()
        assertTrue(exited, "the JVM with a stuck shutdown did not exit")
        assertEquals(ShutdownWatchdog.HALT_STATUS, process.exitValue())
    }

    private companion object {
        /** Longest the test waits for the child, far more than its watchdog timeout. */
        const val WAIT_S = 30L
    }
}

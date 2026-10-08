package org.mjdev.desktop.helpers.font.guard

import java.util.concurrent.TimeUnit

/**
 * Stops a child process for sure: asks it politely (SIGTERM) and kills it (SIGKILL) when it has
 * not exited within the grace period. A desktop child whose UI thread deadlocks during shutdown
 * would otherwise stay alive as an orphan nobody can stop.
 */
object ChildProcessStopper {
    /** Time the child gets to shut down cleanly before it is killed, in milliseconds. */
    const val GRACE_MS: Long = 5_000L

    /** Time to wait for the kill to take effect, in milliseconds. */
    private const val KILL_WAIT_MS: Long = 2_000L

    /** Stops [process]; returns true when it had to be killed forcibly. */
    fun stop(
        process: Process,
        graceMs: Long = GRACE_MS,
    ): Boolean {
        if (!process.isAlive) return false
        process.destroy()
        if (process.waitFor(graceMs, TimeUnit.MILLISECONDS)) return false
        process.destroyForcibly()
        process.waitFor(KILL_WAIT_MS, TimeUnit.MILLISECONDS)
        return true
    }
}

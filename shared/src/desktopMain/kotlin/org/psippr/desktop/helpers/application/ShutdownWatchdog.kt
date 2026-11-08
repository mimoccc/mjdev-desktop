package org.psippr.desktop.helpers.application

/**
 * Makes sure a started shutdown ends. When the UI thread hangs in native code while holding the
 * AWT lock (skiko `setSwapInterval`, see the runtime-modes notes), AWT's own shutdown waits for
 * that lock forever and the desktop can no longer be stopped at all, not from the IDE, not by
 * Ctrl+C, not by its window. The watchdog halts the JVM once a shutdown has run [timeoutMs].
 */
object ShutdownWatchdog {
    /**
     * Time a shutdown may take before the JVM is halted, in milliseconds. Longer than the font
     * guard parent needs to stop its child (ChildProcessStopper: 5 s grace + 2 s kill wait), so the
     * parent is never halted before its child is gone.
     */
    const val TIMEOUT_MS: Long = 10_000L

    /** Exit status of a halted shutdown (128 + SIGTERM, like a process ended by a signal). */
    const val HALT_STATUS = 143

    /** Name of the watchdog thread, to find it in thread dumps. */
    private const val THREAD_NAME = "psippr-shutdown-watchdog"

    /** Installs the watchdog; call once, early in main. */
    fun install(
        timeoutMs: Long = TIMEOUT_MS,
        haltStatus: Int = HALT_STATUS,
    ) {
        val watchdog =
            Thread({
                // a normal shutdown ends the JVM during this sleep; only a stuck one gets past it
                runCatching { Thread.sleep(timeoutMs) }
                System.err.println("psippr: shutdown stuck for $timeoutMs ms, halting")
                Runtime.getRuntime().halt(haltStatus)
            }, THREAD_NAME)
        watchdog.isDaemon = true
        Runtime.getRuntime().addShutdownHook(watchdog)
    }
}

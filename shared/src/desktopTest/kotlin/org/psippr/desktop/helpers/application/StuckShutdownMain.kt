package org.psippr.desktop.helpers.application

/** Child JVM of [ShutdownWatchdogTest]: a shutdown hook that never ends, like the AWT deadlock. */
object StuckShutdownMain {
    /** Watchdog timeout of the child, short so the test is quick. */
    private const val TIMEOUT_MS = 500L

    @JvmStatic
    fun main(args: Array<String>) {
        ShutdownWatchdog.install(TIMEOUT_MS)
        Runtime.getRuntime().addShutdownHook(Thread { Thread.sleep(Long.MAX_VALUE) })
        System.exit(0)
    }
}

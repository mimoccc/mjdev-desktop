package org.mjdev.desktop.managers.volume.pactl

import org.mjdev.desktop.log.Log
import kotlin.concurrent.thread

/**
 * Event driven watch of the sound system: one long running `pactl subscribe` that prints a line
 * only when something changes, instead of re-reading everything on a timer.
 * Only device level events count (sink / source / card / server); per-stream events
 * (`sink-input`, `source-output`) fire constantly during playback and are ignored.
 */
class PactlEvents private constructor(
    private val process: Process,
) : AutoCloseable {
    /** Stops watching (ends the `pactl subscribe` process). */
    override fun close() {
        process.destroy()
    }

    companion object {
        /** pactl sub-command streaming events. */
        private const val CMD_SUBSCRIBE = "subscribe"

        /** Device level event lines, e.g. `Event 'change' on sink #58`. */
        private val DEVICE_EVENT = Regex("""^Event '\w+' on (sink|source|card|server) #""")

        /** Name of the reader thread. */
        private const val THREAD_NAME = "pactl-events"

        /** True when [line] is a device level event. */
        fun isDeviceEvent(line: String): Boolean = DEVICE_EVENT.containsMatchIn(line)

        /** Starts watching; [onChange] runs on the reader thread for every device level event. */
        fun start(onChange: () -> Unit): AutoCloseable = runCatching {
            val process = Pactl.start(CMD_SUBSCRIBE)
            thread(name = THREAD_NAME, isDaemon = true) {
                runCatching {
                    process.inputStream.bufferedReader().useLines { lines ->
                        lines.filter(::isDeviceEvent).forEach { onChange() }
                    }
                }
            }
            PactlEvents(process)
        }.getOrElse { e ->
            Log.w("pactl subscribe unavailable: ${e.message}")
            AutoCloseable {}
        }
    }
}

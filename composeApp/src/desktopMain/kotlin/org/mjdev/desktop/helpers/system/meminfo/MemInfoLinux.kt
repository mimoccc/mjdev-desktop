package org.mjdev.desktop.helpers.system.meminfo

import okio.Path.Companion.toPath
import org.mjdev.desktop.extensions.PathExt.lines
import org.mjdev.desktop.context.IDesktopContext

class MemInfoLinux(
    context: IDesktopContext
): MemInfoStub(context) {
    private val info: List<String> = "/proc/meminfo".toPath().lines

    override val free: Double = info.parseLine("MemAvailable").times(KB)

    override val total: Double = info.parseLine("MemTotal").times(KB)

    companion object {
        const val KB = 1024

        private fun List<String>.parseLine(
            filter: String
        ): Double = firstOrNull { l ->
            l.startsWith(filter)
        }?.filter { l ->
            l.isDigit()
        }?.toDouble() ?: 0.0
    }
}
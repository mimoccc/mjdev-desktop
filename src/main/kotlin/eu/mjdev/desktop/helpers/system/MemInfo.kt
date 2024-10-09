package eu.mjdev.desktop.helpers.system

import eu.mjdev.desktop.extensions.Custom.lines
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
class MemInfo(
    private val info: List<String> = File("/proc/meminfo").lines,
    val free: Double = info.parseLine("MemAvailable").times(KB),
    val total: Double = info.parseLine("MemTotal").times(KB),
    val used: Double = total - free,
) {
    companion object {
        const val KB = 1024

        fun List<String>.parseLine(
            filter: String
        ): Double = firstOrNull { l -> l.startsWith(filter) }?.filter { it.isDigit() }?.toDouble() ?: 0.0

        fun Double.toReadable(): String {
            val bytes = toLong()
            return when {
                bytes == Long.MIN_VALUE || bytes < 0 -> "-"
                bytes < 1024L -> "$bytes B"
                bytes <= 0xfffccccccccccccL shr 40 -> "%.1f KB".format(bytes.toDouble() / (0x1 shl 10))
                bytes <= 0xfffccccccccccccL shr 30 -> "%.1f MB".format(bytes.toDouble() / (0x1 shl 20))
                bytes <= 0xfffccccccccccccL shr 20 -> "%.1f GB".format(bytes.toDouble() / (0x1 shl 30))
                bytes <= 0xfffccccccccccccL shr 10 -> "%.1f TB".format(bytes.toDouble() / (0x1 shl 40))
                bytes <= 0xfffccccccccccccL -> "%.1f PB".format((bytes shr 10).toDouble() / (0x1 shl 40))
                else -> "%.1f EB".format((bytes shr 20).toDouble() / (0x1 shl 40))
            }
        }
    }
}
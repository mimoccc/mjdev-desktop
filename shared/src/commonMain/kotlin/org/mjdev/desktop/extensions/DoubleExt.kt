package org.mjdev.desktop.extensions

import kotlin.math.roundToLong

object DoubleExt {
    fun Double.toMemorySizeReadable(): String {
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

    // Minimal KMP-safe replacement for the JVM-only String.format: this object only ever uses the
    // "%.1f" token (one decimal place), so substitute that with a hand-rounded one-decimal number.
    fun String.format(dbl: Double): String {
        val scaled = (dbl * 10.0).roundToLong()
        val sign = if (scaled < 0) "-" else ""
        val abs = if (scaled < 0) -scaled else scaled
        return replace("%.1f", "$sign${abs / 10}.${abs % 10}")
    }
}

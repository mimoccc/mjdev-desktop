package org.mjdev.desktop.extensions

import androidx.compose.ui.graphics.Color

@Suppress("MemberVisibilityCanBePrivate")
@OptIn(ExperimentalStdlibApi::class)
object StringExt {
    fun Byte.toHex(
        upperCase: Boolean = false,
        prefix: String = "#",
        numberRemoveLeadingZeros: Boolean = true,
    ): String =
        toHexString(
            HexFormat {
                this.upperCase = upperCase
                number.prefix = prefix
                number.removeLeadingZeros = numberRemoveLeadingZeros
                number.minLength = 2
            },
        )

    fun Int.toHex(
        upperCase: Boolean = false,
        prefix: String = "#",
        numberRemoveLeadingZeros: Boolean = true,
    ): String =
        toHexString(
            HexFormat {
                this.upperCase = upperCase
                number.prefix = prefix
                number.removeLeadingZeros = numberRemoveLeadingZeros
                number.minLength = 4
            },
        )

    fun Long.toHex(
        upperCase: Boolean = false,
        prefix: String = "#",
        numberRemoveLeadingZeros: Boolean = true,
    ): String =
        toHexString(
            HexFormat {
                this.upperCase = upperCase
                number.prefix = prefix
                number.removeLeadingZeros = numberRemoveLeadingZeros
                number.minLength = 4
            },
        )

    fun Color.toHex(includeAlpha: Boolean = true): String =
        StringBuilder()
            .apply {
                val a = (alpha * 255).toInt()
                val r = (red * 255).toInt()
                val g = (green * 255).toInt()
                val b = (blue * 255).toInt()
                append("#")
                if (includeAlpha) {
                    append(a.toByte().toHex(true, "", false))
                }
                append(r.toByte().toHex(true, "", false))
                append(g.toByte().toHex(true, "", false))
                append(b.toByte().toHex(true, "", false))
            }.toString()

    fun Color.toHexRGB(): String = toHex(false)

    fun Color.toHexARGB(): String = toHex(true)

    val Color.hexRgb get() = toHexRGB()

    val Color.hexRgba get() = toHexARGB()
}

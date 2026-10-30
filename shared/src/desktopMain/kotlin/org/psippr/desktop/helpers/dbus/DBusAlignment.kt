package org.psippr.desktop.helpers.dbus

/** Byte alignment of each D-Bus type, as the specification ("Marshaling") defines it. */
object DBusAlignment {
    private const val ONE = 1
    private const val TWO = 2
    private const val FOUR = 4
    private const val EIGHT = 8

    /** Alignment of the type starting with [code]. */
    fun of(code: Char): Int = when (code) {
        'y', 'g', 'v' -> ONE
        'n', 'q' -> TWO
        'b', 'i', 'u', 's', 'o', 'a', 'h' -> FOUR
        'x', 't', 'd', '(', '{' -> EIGHT
        else -> throw IllegalArgumentException("unknown D-Bus type $code")
    }
}

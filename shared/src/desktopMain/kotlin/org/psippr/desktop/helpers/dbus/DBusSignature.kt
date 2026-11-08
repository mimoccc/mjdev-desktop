package org.psippr.desktop.helpers.dbus

/** Splits D-Bus type signatures (the D-Bus specification, "Type System") into single types. */
object DBusSignature {
    /** Container type codes. */
    const val ARRAY = 'a'
    const val STRUCT_OPEN = '('
    const val STRUCT_CLOSE = ')'
    const val DICT_OPEN = '{'
    const val DICT_CLOSE = '}'

    /** The complete single types of [signature] in order, e.g. `sa{sv}i` -> `s`, `a{sv}`, `i`. */
    fun split(signature: String): List<String> {
        val types = mutableListOf<String>()
        var index = 0
        while (index < signature.length) {
            val end = endOf(signature, index)
            types.add(signature.substring(index, end))
            index = end
        }
        return types
    }

    /** End index (exclusive) of the single complete type starting at [start]. */
    fun endOf(
        signature: String,
        start: Int,
    ): Int = when (signature[start]) {
        ARRAY -> endOf(signature, start + 1)
        STRUCT_OPEN -> closing(signature, start, STRUCT_OPEN, STRUCT_CLOSE)
        DICT_OPEN -> closing(signature, start, DICT_OPEN, DICT_CLOSE)
        else -> start + 1
    }

    // index after the bracket closing the one at start, nested brackets counted
    private fun closing(
        signature: String,
        start: Int,
        open: Char,
        close: Char,
    ): Int {
        var depth = 0
        var index = start
        while (index < signature.length) {
            when (signature[index]) {
                open -> depth++
                close -> if (--depth == 0) return index + 1
            }
            index++
        }
        throw IllegalArgumentException("unbalanced signature: $signature")
    }
}

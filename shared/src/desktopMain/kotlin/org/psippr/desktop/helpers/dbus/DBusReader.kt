package org.psippr.desktop.helpers.dbus

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Decodes D-Bus marshalled values from [bytes]. Offsets are counted from the start of the
 * message, so alignment works across header and body. Types map to: `y` Byte, `b` Boolean,
 * `n` / `q` / `i` / `h` Int, `u` / `x` / `t` Long, `d` Double, `s` / `o` / `g` String, arrays to
 * List (dictionaries to Map), structs to List, variants to [DBusVariant].
 */
class DBusReader(
    bytes: ByteArray,
    order: ByteOrder,
    start: Int = 0,
) {
    private val buffer: ByteBuffer = ByteBuffer.wrap(bytes).order(order)

    init {
        buffer.position(start)
    }

    /** Current offset. */
    val position: Int
        get() = buffer.position()

    /** Reads one value of each single type of [signature]. */
    fun readAll(signature: String): List<Any?> = DBusSignature.split(signature).map(::read)

    /** Reads one value of the single complete type [type]. */
    fun read(type: String): Any? {
        align(DBusAlignment.of(type[0]))
        return when (type[0]) {
            'y' -> buffer.get()
            'b' -> buffer.int != 0
            'n' -> buffer.short.toInt()
            'q' -> buffer.short.toInt() and U16_MASK
            'i', 'h' -> buffer.int
            'u' -> buffer.int.toLong() and U32_MASK
            'x', 't' -> buffer.long
            'd' -> buffer.double
            's', 'o' -> string(buffer.int)
            'g' -> string(buffer.get().toInt() and U8_MASK)
            'v' -> variant()
            DBusSignature.ARRAY -> array(type.substring(1))
            // a dict entry {kv} is laid out like a struct (kv)
            DBusSignature.STRUCT_OPEN, DBusSignature.DICT_OPEN ->
                DBusSignature.split(type.substring(1, type.length - 1)).map(::read)
            else -> throw IllegalArgumentException("cannot read D-Bus type $type")
        }
    }

    private fun variant(): DBusVariant {
        val signature = read("g") as String
        return DBusVariant(signature, read(signature))
    }

    // the length counts the elements only; the padding before the first element is not in it
    private fun array(element: String): Any {
        val length = buffer.int
        align(DBusAlignment.of(element[0]))
        val end = buffer.position() + length
        val values = mutableListOf<Any?>()
        while (buffer.position() < end) values.add(read(element))
        if (element[0] != DBusSignature.DICT_OPEN) return values
        // a{kv}: entries are decoded like structs, turned into a map
        return values.filterIsInstance<List<*>>().associate { entry -> entry[0] to entry[1] }
    }

    private fun string(length: Int): String {
        val bytes = ByteArray(length)
        buffer.get(bytes)
        buffer.get() // trailing NUL
        return String(bytes, Charsets.UTF_8)
    }

    /** Skips padding up to the next multiple of [alignment]. */
    fun align(alignment: Int) {
        val remainder = buffer.position() % alignment
        if (remainder != 0) buffer.position(buffer.position() + alignment - remainder)
    }

    private companion object {
        const val U8_MASK = 0xFF
        const val U16_MASK = 0xFFFF
        const val U32_MASK = 0xFFFFFFFFL
    }
}

package org.psippr.desktop.helpers.dbus

import java.io.ByteArrayOutputStream

/**
 * Encodes values in D-Bus little endian marshalling. Takes the same Kotlin types [DBusReader]
 * gives (numbers may be any Number), so a message can be read back exactly as written.
 */
class DBusWriter {
    private val out = ByteArrayOutputStream()

    /** Bytes written so far; offsets count from the start of the message. */
    val size: Int
        get() = out.size()

    /** The encoded bytes. */
    fun bytes(): ByteArray = out.toByteArray()

    /** Writes [values], one per single type of [signature]. */
    fun writeAll(
        signature: String,
        values: List<Any?>,
    ) {
        DBusSignature.split(signature).zip(values).forEach { (type, value) -> write(type, value) }
    }

    /** Writes [value] as the single complete type [type]. */
    fun write(
        type: String,
        value: Any?,
    ) {
        align(DBusAlignment.of(type[0]))
        when (type[0]) {
            'y' -> out.write((value as Number).toInt())
            'b' -> int(if (value == true) 1 else 0)
            'n', 'q' -> short((value as Number).toInt())
            'i', 'h', 'u' -> int((value as Number).toInt())
            'x', 't' -> long((value as Number).toLong())
            'd' -> long(java.lang.Double.doubleToLongBits((value as Number).toDouble()))
            's', 'o' -> string(value as String)
            'g' -> signature(value as String)
            'v' ->
                (value as DBusVariant).let { variant ->
                    signature(variant.signature).also { write(variant.signature, variant.value) }
                }
            DBusSignature.ARRAY -> array(type.substring(1), value)
            // a dict entry {kv} is laid out like a struct (kv)
            DBusSignature.STRUCT_OPEN, DBusSignature.DICT_OPEN ->
                DBusSignature.split(type.substring(1, type.length - 1)).zip(value as List<*>).forEach { (t, v) ->
                    write(t, v)
                }
            else -> throw IllegalArgumentException("cannot write D-Bus type $type")
        }
    }

    // length placeholder, padding to the element alignment, elements, then the real length
    private fun array(
        element: String,
        value: Any?,
    ) {
        val lengthAt = size
        int(0)
        align(DBusAlignment.of(element[0]))
        val start = size
        val items: List<Any?> =
            if (element[0] == DBusSignature.DICT_OPEN) {
                (value as Map<*, *>).map { (k, v) -> listOf(k, v) }
            } else {
                value as List<*>
            }
        items.forEach { item -> write(element, item) }
        patchInt(lengthAt, size - start)
    }

    private fun string(text: String) {
        val bytes = text.toByteArray(Charsets.UTF_8)
        int(bytes.size)
        out.write(bytes)
        out.write(0)
    }

    private fun signature(text: String) {
        val bytes = text.toByteArray(Charsets.US_ASCII)
        out.write(bytes.size)
        out.write(bytes)
        out.write(0)
    }

    /** Pads with zeros up to the next multiple of [alignment]. */
    fun align(alignment: Int) {
        while (size % alignment != 0) out.write(0)
    }

    private fun short(value: Int) {
        out.write(value and BYTE_MASK)
        out.write((value shr BYTE_BITS) and BYTE_MASK)
    }

    private fun int(value: Int) {
        repeat(INT_BYTES) { index -> out.write((value shr (index * BYTE_BITS)) and BYTE_MASK) }
    }

    private fun long(value: Long) {
        repeat(LONG_BYTES) { index -> out.write(((value shr (index * BYTE_BITS)) and BYTE_MASK.toLong()).toInt()) }
    }

    // rewrites 4 bytes at offset (array lengths are known only after the elements)
    private fun patchInt(
        offset: Int,
        value: Int,
    ) {
        val bytes = out.toByteArray()
        repeat(INT_BYTES) { index -> bytes[offset + index] = ((value shr (index * BYTE_BITS)) and BYTE_MASK).toByte() }
        out.reset()
        out.write(bytes)
    }

    private companion object {
        const val BYTE_MASK = 0xFF
        const val BYTE_BITS = 8
        const val INT_BYTES = 4
        const val LONG_BYTES = 8
    }
}

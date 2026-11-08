package org.psippr.desktop.helpers.dbus

import java.io.DataInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Turns [DBusMessage]s into bytes and back: the 12 byte fixed header, the header fields array
 * `a(yv)`, padding to 8, then the body (D-Bus specification, "Message Format").
 */
object DBusCodec {
    private const val LITTLE = 'l'.code
    private const val BIG = 'B'.code
    private const val PROTOCOL_VERSION = 1
    private const val FIXED_HEADER = 12
    private const val FIELDS_LENGTH_SIZE = 4
    private const val HEADER_ALIGNMENT = 8
    private const val FIELDS_SIGNATURE = "a(yv)"

    private const val FIELD_PATH = 1
    private const val FIELD_INTERFACE = 2
    private const val FIELD_MEMBER = 3
    private const val FIELD_ERROR_NAME = 4
    private const val FIELD_REPLY_SERIAL = 5
    private const val FIELD_DESTINATION = 6
    private const val FIELD_SENDER = 7
    private const val FIELD_SIGNATURE = 8

    /** [message] as bytes, little endian. */
    fun encode(message: DBusMessage): ByteArray {
        val body = DBusWriter().apply { writeAll(message.signature, message.body) }.bytes()
        val header = DBusWriter()
        header.write("y", LITTLE)
        header.write("y", message.type)
        header.write("y", message.flags)
        header.write("y", PROTOCOL_VERSION)
        header.write("u", body.size)
        header.write("u", message.serial)
        header.write(FIELDS_SIGNATURE, fields(message))
        header.align(HEADER_ALIGNMENT)
        return header.bytes() + body
    }

    /** Reads one whole message from [input]; blocks until it is complete. */
    fun decode(input: InputStream): DBusMessage {
        val data = DataInputStream(input)
        val start = ByteArray(FIXED_HEADER + FIELDS_LENGTH_SIZE)
        data.readFully(start)
        val order = if (start[0].toInt() == BIG) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN
        val fixed = ByteBuffer.wrap(start).order(order)
        val bodyLength = fixed.getInt(4)
        val fieldsLength = fixed.getInt(FIXED_HEADER)
        val headerEnd = padded(FIXED_HEADER + FIELDS_LENGTH_SIZE + fieldsLength)
        val bytes = start + ByteArray(headerEnd - start.size + bodyLength).also { data.readFully(it) }
        val head = ByteBuffer.wrap(bytes).order(order)
        val fieldList = DBusReader(bytes, order, FIXED_HEADER).read(FIELDS_SIGNATURE) as List<*>
        val fields =
            fieldList.filterIsInstance<List<*>>().associate {
                (it[0] as Byte).toInt() to
                    (it[1] as DBusVariant).value
            }
        val signature = fields[FIELD_SIGNATURE] as String? ?: ""
        return DBusMessage(
            type = bytes[1].toInt(),
            flags = bytes[2].toInt(),
            serial = head.getInt(8).toLong() and U32_MASK,
            path = fields[FIELD_PATH] as String?,
            interfaceName = fields[FIELD_INTERFACE] as String?,
            member = fields[FIELD_MEMBER] as String?,
            errorName = fields[FIELD_ERROR_NAME] as String?,
            replySerial = fields[FIELD_REPLY_SERIAL] as Long?,
            destination = fields[FIELD_DESTINATION] as String?,
            sender = fields[FIELD_SENDER] as String?,
            signature = signature,
            body = if (signature.isEmpty()) emptyList() else DBusReader(bytes, order, headerEnd).readAll(signature),
        )
    }

    // header fields present in the message, each as (code, variant)
    private fun fields(message: DBusMessage): List<List<Any>> = buildList {
        message.path?.let { add(listOf(FIELD_PATH.toByte(), DBusVariant("o", it))) }
        message.interfaceName?.let { add(listOf(FIELD_INTERFACE.toByte(), DBusVariant("s", it))) }
        message.member?.let { add(listOf(FIELD_MEMBER.toByte(), DBusVariant("s", it))) }
        message.errorName?.let { add(listOf(FIELD_ERROR_NAME.toByte(), DBusVariant("s", it))) }
        message.replySerial?.let { add(listOf(FIELD_REPLY_SERIAL.toByte(), DBusVariant("u", it))) }
        message.destination?.let { add(listOf(FIELD_DESTINATION.toByte(), DBusVariant("s", it))) }
        message.sender?.let { add(listOf(FIELD_SENDER.toByte(), DBusVariant("s", it))) }
        if (message.signature.isNotEmpty()) add(listOf(FIELD_SIGNATURE.toByte(), DBusVariant("g", message.signature)))
    }

    private fun padded(length: Int): Int = (length + HEADER_ALIGNMENT - 1) / HEADER_ALIGNMENT * HEADER_ALIGNMENT

    private const val U32_MASK = 0xFFFFFFFFL
}

package org.psippr.desktop.helpers.dbus

import java.io.InputStream
import java.io.OutputStream
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.Channels
import java.nio.channels.SocketChannel
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong

/**
 * A connection to a D-Bus bus over its unix socket: EXTERNAL authentication (the kernel tells the
 * bus who we are), `Hello`, then messages both ways. Small on purpose: just what a service that
 * owns a name and answers calls needs, without a third party library.
 */
class DBusConnection private constructor(
    private val channel: SocketChannel,
) : AutoCloseable {
    private val output: OutputStream = Channels.newOutputStream(channel)
    private val input: InputStream = Channels.newInputStream(channel)
    private val serials = AtomicLong(0)

    /** Unique bus name the bus gave this connection (e.g. `:1.42`). */
    var uniqueName: String = ""
        private set

    /** Next message serial (never 0). */
    fun nextSerial(): Long = serials.incrementAndGet()

    /** Sends [message]. */
    @Synchronized
    fun send(message: DBusMessage) {
        output.write(DBusCodec.encode(message))
        output.flush()
    }

    /** Reads the next message; blocks. */
    fun receive(): DBusMessage = DBusCodec.decode(input)

    /** Calls a method of the bus itself and waits for its reply (used before serving starts). */
    fun callBus(
        member: String,
        signature: String = "",
        body: List<Any?> = emptyList(),
    ): DBusMessage {
        val serial = nextSerial()
        send(
            DBusMessage(
                type = DBusMessage.TYPE_METHOD_CALL,
                serial = serial,
                path = BUS_PATH,
                interfaceName = BUS_NAME,
                member = member,
                destination = BUS_NAME,
                signature = signature,
                body = body,
            ),
        )
        while (true) {
            val message = receive()
            if (message.replySerial == serial) return message
        }
    }

    /**
     * Asks for the well-known [name] without taking it from a current owner (so GNOME keeps its
     * notification service); true when this connection now owns it.
     */
    fun requestName(name: String): Boolean {
        val reply = callBus(REQUEST_NAME, "su", listOf(name, DO_NOT_QUEUE))
        return reply.type == DBusMessage.TYPE_METHOD_RETURN && reply.body.firstOrNull() == PRIMARY_OWNER
    }

    override fun close() {
        runCatching { channel.close() }
    }

    // SASL: NUL byte, AUTH EXTERNAL with the uid as hex of its decimal digits, OK, BEGIN
    private fun authenticate() {
        val uid = Files.getAttribute(Path.of(PROC_SELF), UID_ATTRIBUTE).toString()
        val hexUid = uid.toByteArray(Charsets.US_ASCII).joinToString("") { "%02x".format(it) }
        output.write(0)
        output.write("$AUTH_EXTERNAL $hexUid\r\n".toByteArray(Charsets.US_ASCII))
        output.flush()
        val answer = lineOf(input)
        check(answer.startsWith(AUTH_OK)) { "D-Bus authentication refused: $answer" }
        output.write("$BEGIN\r\n".toByteArray(Charsets.US_ASCII))
        output.flush()
        uniqueName = callBus(HELLO).body.firstOrNull() as String? ?: ""
    }

    // one CRLF terminated line, read byte by byte so no message bytes are swallowed after it
    private fun lineOf(stream: InputStream): String {
        val bytes = StringBuilder()
        while (true) {
            val next = stream.read()
            if (next < 0 || next == '\n'.code) break
            if (next != '\r'.code) bytes.append(next.toChar())
        }
        return bytes.toString()
    }

    companion object {
        const val BUS_NAME = "org.freedesktop.DBus"
        const val BUS_PATH = "/org/freedesktop/DBus"
        private const val HELLO = "Hello"
        private const val REQUEST_NAME = "RequestName"

        /** RequestName flag: fail instead of waiting in the queue for the name. */
        private const val DO_NOT_QUEUE = 4

        /** RequestName answer: this connection owns the name now. */
        private const val PRIMARY_OWNER = 1L
        private const val AUTH_EXTERNAL = "AUTH EXTERNAL"
        private const val AUTH_OK = "OK"
        private const val BEGIN = "BEGIN"
        private const val PROC_SELF = "/proc/self"
        private const val UID_ATTRIBUTE = "unix:uid"

        /** Connects to the bus at the unix socket [path] and authenticates. */
        fun open(path: Path): DBusConnection {
            val channel = SocketChannel.open(StandardProtocolFamily.UNIX)
            channel.connect(UnixDomainSocketAddress.of(path))
            return DBusConnection(channel).also { connection ->
                runCatching { connection.authenticate() }.onFailure { e ->
                    connection.close()
                    throw e
                }
            }
        }
    }
}

package org.psippr.desktop.helpers.greetd

import com.google.gson.Gson
import java.io.Closeable
import java.io.IOException
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SocketChannel
import java.nio.file.Path

/**
 * Connection to the greetd IPC socket: each message is a 32-bit native-order length followed
 * by that many bytes of UTF-8 JSON (see greetd-ipc(7)).
 */
class GreetdConnection(
    socketPath: Path,
) : Closeable {
    /** Plain gson: null fields are left out, greetd only gets the fields of the request type. */
    private val gson = Gson()

    private val channel: SocketChannel =
        SocketChannel.open(StandardProtocolFamily.UNIX).apply {
            connect(UnixDomainSocketAddress.of(socketPath))
        }

    /** Sends [request] and returns greetd's answer. */
    fun send(request: GreetdRequest): GreetdResponse {
        write(gson.toJson(request).toByteArray(Charsets.UTF_8))
        return gson.fromJson(String(read(), Charsets.UTF_8), GreetdResponse::class.java)
    }

    private fun write(payload: ByteArray) {
        val buffer =
            ByteBuffer
                .allocate(LENGTH_BYTES + payload.size)
                .order(ByteOrder.nativeOrder())
                .putInt(payload.size)
                .put(payload)
                .flip()
        while (buffer.hasRemaining()) channel.write(buffer)
    }

    private fun read(): ByteArray {
        val length = readFully(LENGTH_BYTES).order(ByteOrder.nativeOrder()).int
        if (length !in 0..MAX_PAYLOAD_BYTES) throw IOException("$BAD_LENGTH$length")
        return readFully(length).array()
    }

    private fun readFully(size: Int): ByteBuffer {
        val buffer = ByteBuffer.allocate(size)
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) < 0) throw IOException(CLOSED)
        }
        return buffer.flip()
    }

    override fun close() = channel.close()

    private companion object {
        /** Size of the length prefix. */
        const val LENGTH_BYTES = Int.SIZE_BYTES

        /** Upper bound of a sane reply, guards against a corrupt length prefix. */
        const val MAX_PAYLOAD_BYTES = 1 shl 20

        const val BAD_LENGTH = "greetd reply with invalid length "
        const val CLOSED = "greetd closed the connection"
    }
}

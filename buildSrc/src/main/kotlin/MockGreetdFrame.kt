import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SocketChannel

/**
 * greetd IPC framing: a native-endian 32 bit payload length, then the JSON payload in UTF-8.
 */
object MockGreetdFrame {
    /** Largest payload accepted, far above any greeter request. */
    private const val MAX_PAYLOAD_BYTES = 65_536

    /** Reads one message from [channel]; null when the greeter closed the connection. */
    fun read(channel: SocketChannel): String? {
        val length = ByteBuffer.allocate(Int.SIZE_BYTES).order(ByteOrder.nativeOrder())
        if (!fill(channel, length)) return null
        val size = length.flip().int
        if (size !in 0..MAX_PAYLOAD_BYTES) return null
        val payload = ByteBuffer.allocate(size)
        return if (fill(channel, payload)) String(payload.array(), Charsets.UTF_8) else null
    }

    /** Writes the message [json] to [channel]. */
    fun write(
        channel: SocketChannel,
        json: String,
    ) {
        val bytes = json.toByteArray(Charsets.UTF_8)
        val frame =
            ByteBuffer
                .allocate(Int.SIZE_BYTES + bytes.size)
                .order(ByteOrder.nativeOrder())
                .putInt(bytes.size)
                .put(bytes)
                .flip()
        while (frame.hasRemaining()) channel.write(frame)
    }

    // false when the connection ended before the buffer was full
    private fun fill(
        channel: SocketChannel,
        buffer: ByteBuffer,
    ): Boolean {
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) < 0) return false
        }
        return true
    }
}

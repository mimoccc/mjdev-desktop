package eu.mjdev.desktop.helpers.adb.helpers

import okio.*
import java.lang.Integer.min
import java.nio.ByteBuffer

class AdbStreamImpl internal constructor(
    private val messageQueue: AdbMessageQueue,
    private val adbWriter: AdbWriter,
    private val maxPayloadSize: Int,
    private val localId: Int,
    private val remoteId: Int
) : IAdbStream {
    private var isClosed = false

    override val source = object : Source {
        private var message: AdbMessage? = null
        private var bytesRead = 0

        override fun read(sink: Buffer, byteCount: Long): Long {
            val message = message() ?: return -1
            val bytesRemaining = message.payloadLength - bytesRead
            val bytesToRead = min(byteCount.toInt(), bytesRemaining)
            sink.write(message.payload, bytesRead, bytesToRead)
            bytesRead += bytesToRead
            check(bytesRead <= message.payloadLength)
            if (bytesRead == message.payloadLength) {
                this.message = null
                adbWriter.writeOkay(localId, remoteId)
            }
            return bytesToRead.toLong()
        }

        private fun message(): AdbMessage? {
            message?.let { return it }
            val nextMessage = nextMessage(AdbConstants.CMD_WRTE)
            message = nextMessage
            bytesRead = 0
            return nextMessage
        }

        override fun close() {}

        override fun timeout() = Timeout.NONE
    }.buffer()

    override val sink = object : Sink {
        private val buffer = ByteBuffer.allocate(maxPayloadSize)

        override fun write(source: Buffer, byteCount: Long) {
            var remainingBytes = byteCount
            while (true) {
                remainingBytes -= writeToBuffer(source, byteCount)
                if (remainingBytes == 0L) return
                check(remainingBytes > 0L)
            }
        }

        private fun writeToBuffer(source: BufferedSource, byteCount: Long): Int {
            val bytesToWrite = min(buffer.remaining(), byteCount.toInt())
            val bytesWritten = source.read(buffer.array(), buffer.position(), bytesToWrite)
            buffer.position(buffer.position() + bytesWritten)
            if (buffer.remaining() == 0) flush()
            return bytesWritten
        }

        override fun flush() {
            adbWriter.writeWrite(localId, remoteId, buffer.array(), 0, buffer.position())
            buffer.clear()
        }

        override fun close() {}

        override fun timeout() = Timeout.NONE
    }.buffer()

    @Suppress("SameParameterValue")
    private fun nextMessage(
        command: Int
    ): AdbMessage? {
        return try {
            messageQueue.take(localId, command)
        } catch (_: IOException) {
            close()
            return null
        }
    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        adbWriter.writeClose(localId, remoteId)
        messageQueue.stopListening(localId)
    }
}

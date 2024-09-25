package eu.mjdev.desktop.helpers.adb.helpers

import okio.Sink
import okio.buffer
import java.nio.ByteBuffer

@Suppress("MemberVisibilityCanBePrivate")
internal class AdbWriter(sink: Sink) : AutoCloseable {
    private val bufferedSink = sink.buffer()

    fun writeConnect() = write(
        AdbConstants.CMD_CNXN,
        AdbConstants.CONNECT_VERSION,
        AdbConstants.CONNECT_MAXDATA,
        AdbConstants.CONNECT_PAYLOAD,
        0,
        AdbConstants.CONNECT_PAYLOAD.size
    )

    fun writeAuth(authType: Int, authPayload: ByteArray) = write(
        AdbConstants.CMD_AUTH,
        authType,
        0,
        authPayload,
        0,
        authPayload.size
    )

    fun writeOpen(localId: Int, destination: String) {
        val destinationBytes = destination.toByteArray()
        val buffer = ByteBuffer.allocate(destinationBytes.size + 1)
        buffer.put(destinationBytes)
        buffer.put(0)
        val payload = buffer.array()
        write(AdbConstants.CMD_OPEN, localId, 0, payload, 0, payload.size)
    }

    fun writeWrite(localId: Int, remoteId: Int, payload: ByteArray, offset: Int, length: Int) {
        write(AdbConstants.CMD_WRTE, localId, remoteId, payload, offset, length)
    }

    fun writeClose(localId: Int, remoteId: Int) {
        write(AdbConstants.CMD_CLSE, localId, remoteId, null, 0, 0)
    }

    fun writeOkay(localId: Int, remoteId: Int) {
        write(AdbConstants.CMD_OKAY, localId, remoteId, null, 0, 0)
    }

    fun write(
        command: Int,
        arg0: Int,
        arg1: Int,
        payload: ByteArray?,
        offset: Int,
        length: Int
    ) {
        log {
            "(${Thread.currentThread().name}) > ${
                AdbMessage(
                    command,
                    arg0,
                    arg1,
                    length,
                    0,
                    0,
                    payload ?: ByteArray(0)
                )
            }"
        }
        synchronized(bufferedSink) {
            bufferedSink.apply {
                writeIntLe(command)
                writeIntLe(arg0)
                writeIntLe(arg1)
                if (payload == null) {
                    writeIntLe(0)
                    writeIntLe(0)
                } else {
                    writeIntLe(length)
                    writeIntLe(payloadChecksum(payload))
                }
                writeIntLe(command xor -0x1)
                if (payload != null) {
                    write(payload, offset, length)
                }
                flush()
            }
        }
    }

    override fun close() {
        bufferedSink.close()
    }

    companion object {
        private fun payloadChecksum(payload: ByteArray): Int {
            var checksum = 0
            for (byte in payload) {
                checksum += byte.toUByte().toInt()
            }
            return checksum
        }
    }
}

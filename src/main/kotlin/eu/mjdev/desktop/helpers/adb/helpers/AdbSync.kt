package eu.mjdev.desktop.helpers.adb.helpers

import okio.Buffer
import okio.Sink
import okio.Source
import java.io.IOException
import java.nio.charset.StandardCharsets

internal const val LIST = "LIST"
internal const val RECV = "RECV"
internal const val SEND = "SEND"
internal const val STAT = "STAT"
internal const val DATA = "DATA"
internal const val DONE = "DONE"
internal const val OKAY = "OKAY"
internal const val QUIT = "QUIT"
internal const val FAIL = "FAIL"

internal val SYNC_IDS = setOf(LIST, RECV, SEND, STAT, DATA, DONE, OKAY, QUIT, FAIL)

private class Packet(val id: String, val arg: Int)

class AdbSyncStream(
    private val stream: AdbStream
) : AutoCloseable {
    private val buffer = Buffer()

    @Throws(IOException::class)
    fun send(source: Source, remotePath: String, mode: Int, lastModifiedMs: Long) {
        val remote = "$remotePath,$mode"
        writePacket(SEND, remote.length)

        stream.sink.apply {
            writeString(remote, StandardCharsets.UTF_8)
            flush()
        }

        buffer.clear()

        while (true) {
            val read = source.read(buffer, 64_000)
            if (read == -1L) break
            writePacket(DATA, read.toInt())
            val sent = stream.sink.writeAll(buffer)
            check(read == sent)
        }

        writePacket(DONE, (lastModifiedMs / 1000).toInt())

        stream.sink.flush()

        val packet = readPacket()
        if (packet.id != OKAY) throw IOException("Unexpected sync packet id: ${packet.id}")
    }

    @Throws(IOException::class)
    fun recv(sink: Sink, remotePath: String) {
        writePacket(RECV, remotePath.length)
        stream.sink.apply {
            writeString(remotePath, StandardCharsets.UTF_8)
            flush()
        }

        buffer.clear()

        while (true) {
            val packet = readPacket()
            if (packet.id == DONE) break
            if (packet.id == FAIL) {
                val message = stream.source.readString(packet.arg.toLong(), StandardCharsets.UTF_8)
                throw IOException("Sync failed: $message")
            }
            if (packet.id != DATA) throw IOException("Unexpected sync packet id: ${packet.id}")
            val chunkSize = packet.arg
            stream.source.readFully(buffer, chunkSize.toLong())
            buffer.readAll(sink)
        }

        sink.flush()
    }

    private fun writePacket(id: String, arg: Int) {
        stream.sink.apply {
            writeString(id, StandardCharsets.UTF_8)
            writeIntLe(arg)
            flush()
        }
    }

    private fun readPacket(): Packet {
        val id = stream.source.readString(4, StandardCharsets.UTF_8)
        val arg = stream.source.readIntLe()
        return Packet(id, arg)
    }

    override fun close() {
        writePacket(QUIT, 0)
        stream.close()
    }
}
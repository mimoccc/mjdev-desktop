package eu.mjdev.desktop.helpers.adb.helpers

import okio.Buffer
import okio.Sink
import okio.Source
import java.io.IOException
import java.nio.charset.StandardCharsets

@Suppress("MemberVisibilityCanBePrivate")
class AdbSyncStream(
    private val stream: IAdbStream
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

    companion object {
        const val LIST = "LIST"
        const val RECV = "RECV"
        const val SEND = "SEND"
        const val STAT = "STAT"
        const val DATA = "DATA"
        const val DONE = "DONE"
        const val OKAY = "OKAY"
        const val QUIT = "QUIT"
        const val FAIL = "FAIL"

        val SYNC_IDS = setOf(LIST, RECV, SEND, STAT, DATA, DONE, OKAY, QUIT, FAIL)
    }
}

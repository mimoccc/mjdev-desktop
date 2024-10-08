package eu.mjdev.desktop.helpers.adb.helpers

import eu.mjdev.desktop.helpers.adb.helpers.AdbShellPacket.*
import java.io.IOException

class AdbShellStream(
    private val stream: IAdbStream
) : AutoCloseable {
    @Throws(IOException::class)
    fun readAll(): AdbShellResponse {
        val output = StringBuilder()
        val errorOutput = StringBuilder()
        while (true) {
            when (val packet = read()) {
                is Exit -> {
                    val exitCode = packet.payload[0].toInt()
                    return AdbShellResponse(output.toString(), errorOutput.toString(), exitCode)
                }

                is StdOut -> {
                    output.append(String(packet.payload))
                }

                is StdError -> {
                    errorOutput.append(String(packet.payload))
                }
            }
        }
    }

    @Throws(IOException::class)
    fun read(): AdbShellPacket {
        stream.source.apply {
            val id = checkId(readByte().toInt())
            val length = checkLength(id, readIntLe())
            val payload = readByteArray(length.toLong())
            return when (id) {
                ID_STDOUT -> StdOut(payload)
                ID_STDERR -> StdError(payload)
                ID_EXIT -> Exit(payload)
                else -> throw IllegalArgumentException("Invalid shell packet id: $id")
            }
        }
    }

    @Throws(IOException::class)
    fun write(string: String) {
        write(ID_STDIN, string.toByteArray())
    }

    @Throws(IOException::class)
    fun write(id: Int, payload: ByteArray? = null) {
        stream.sink.apply {
            writeByte(id)
            writeIntLe(payload?.size ?: 0)
            if (payload != null) write(payload)
            flush()
        }
    }

    override fun close() {
        stream.close()
    }

    private fun checkId(id: Int): Int {
        check(id == ID_STDOUT || id == ID_STDERR || id == ID_EXIT) {
            "Invalid shell packet id: $id"
        }
        return id
    }

    private fun checkLength(id: Int, length: Int): Int {
        check(length >= 0) { "Shell packet length must be >= 0: $length" }
        check(id != ID_EXIT || length == 1) { "Shell exit packet does not have payload length == 1: $length" }
        return length
    }

    companion object {
        const val ID_STDIN = 0
        const val ID_STDOUT = 1
        const val ID_STDERR = 2
        const val ID_EXIT = 3

        @Suppress("unused")
        const val ID_CLOSE_STDIN = 3
    }
}

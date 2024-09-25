package eu.mjdev.desktop.helpers.adb.helpers

import eu.mjdev.desktop.helpers.adb.helpers.AdbShellStream.Companion.ID_EXIT
import eu.mjdev.desktop.helpers.adb.helpers.AdbSyncStream.Companion.SYNC_IDS
import okio.Buffer
import okio.BufferedSource
import java.nio.charset.StandardCharsets

@Suppress("unused")
internal class AdbMessage(
    val command: Int,
    val arg0: Int,
    val arg1: Int,
    val payloadLength: Int,
    val checksum: Int,
    val magic: Int,
    val payload: ByteArray
) {
    override fun toString() =
        "${commandStr()}[${argStr(arg0)}, ${argStr(arg1)}] ${payloadStr()}"

    private fun payloadStr(): String {
        if (payloadLength == 0) return ""
        return when (command) {
            AdbConstants.CMD_AUTH -> if (arg0 == AdbConstants.AUTH_TYPE_RSA_PUBLIC) String(payload) else "auth[${payloadLength}]"
            AdbConstants.CMD_WRTE -> writePayloadStr()
            AdbConstants.CMD_OPEN -> String(payload, 0, payloadLength - 1)
            else -> "payload[$payloadLength]"
        }
    }

    private fun writePayloadStr(): String {
        shellPayloadStr()?.let { return it }
        syncPayloadStr()?.let { return it }
        return "payload[$payloadLength]"
    }

    private fun shellPayloadStr(): String? {
        val source: BufferedSource = getSource()
        if (source.buffer.size < 5) return null
        val id = source.readByte().toInt()
        if (id < 0 || id > 3) return null
        val length = source.readIntLe()
        if (length != source.buffer.size.toInt()) return null
        if (id == ID_EXIT) return "[shell] exit(${source.readByte()})"
        val payload = String(payload, 5, payloadLength - 5)
        return "[shell] $payload"
    }

    private fun syncPayloadStr(): String? {
        val source: BufferedSource = getSource()
        if (source.buffer.size < 8) return null
        val id = source.readString(4, StandardCharsets.UTF_8)
        if (id !in SYNC_IDS) return null
        val arg = source.readIntLe()
        return "[sync] $id($arg)"
    }

    private fun getSource(): BufferedSource {
        return Buffer().apply { write(payload, 0, payloadLength) }
    }

    private fun argStr(arg: Int) = String.format("%X", arg)

    private fun commandStr() = when (command) {
        AdbConstants.CMD_AUTH -> "AUTH"
        AdbConstants.CMD_CNXN -> "CNXN"
        AdbConstants.CMD_OPEN -> "OPEN"
        AdbConstants.CMD_OKAY -> "OKAY"
        AdbConstants.CMD_CLSE -> "CLSE"
        AdbConstants.CMD_WRTE -> "WRTE"
        else -> "????"
    }
}

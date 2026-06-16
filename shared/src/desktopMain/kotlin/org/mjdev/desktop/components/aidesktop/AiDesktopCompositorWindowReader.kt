package org.mjdev.desktop.components.aidesktop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mjdev.desktop.log.Log
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class AiDesktopCompositorWindowReader private constructor() {
    companion object {
        private const val XdgRuntimeDir = "XDG_RUNTIME_DIR"
        private const val SocketFileName = "mjdev-compositor.sock"
        private const val ListWindowsRequest = "{\"cmd\":\"list-windows\"}\n"
        private const val BufferSize = 8192

        suspend fun readWindows(): List<AiDesktopWindowBounds> =
            withContext(Dispatchers.IO) {
                val path = socketPath() ?: return@withContext emptyList()
                runCatching {
                    SocketChannel.open(StandardProtocolFamily.UNIX).use { channel ->
                        channel.connect(UnixDomainSocketAddress.of(path))
                        channel.write(ByteBuffer.wrap(ListWindowsRequest.toByteArray(StandardCharsets.UTF_8)))
                        parseWindows(readLine(channel))
                    }
                }.onFailure { e ->
                    Log.w("AiDesktopCompositorWindowReader: ${e.message}")
                }.getOrDefault(emptyList())
            }

        private fun socketPath(): Path? {
            val dir = System.getenv(XdgRuntimeDir) ?: return null
            val path = Path.of(dir, SocketFileName)
            return if (Files.exists(path)) path else null
        }

        private fun readLine(channel: SocketChannel): String {
            val buffer = ByteBuffer.allocate(BufferSize)
            val pending = StringBuilder()
            while (true) {
                buffer.clear()
                val read = channel.read(buffer)
                if (read <= 0) {
                    return pending.toString()
                }
                buffer.flip()
                pending.append(StandardCharsets.UTF_8.decode(buffer))
                val newline = pending.indexOf("\n")
                if (newline >= 0) {
                    return pending.substring(0, newline)
                }
            }
        }

        private fun parseWindows(line: String): List<AiDesktopWindowBounds> =
            line
                .substringAfter("\"windows\":[", missingDelimiterValue = "")
                .substringBeforeLast("]", missingDelimiterValue = "")
                .split("},{")
                .mapNotNull { rawWindow ->
                    val source = rawWindow.trim('{', '}')
                    val id = readLong(source, "\"id\":") ?: return@mapNotNull null
                    val x = readInt(source, "\"x\":") ?: return@mapNotNull null
                    val y = readInt(source, "\"y\":") ?: return@mapNotNull null
                    val width = readInt(source, "\"width\":") ?: return@mapNotNull null
                    val height = readInt(source, "\"height\":") ?: return@mapNotNull null
                    AiDesktopWindowBounds(
                        id = id,
                        x = x,
                        y = y,
                        width = width,
                        height = height,
                    )
                }

        private fun readInt(
            source: String,
            key: String,
        ): Int? = readNumber(source, key)?.toIntOrNull()

        private fun readLong(
            source: String,
            key: String,
        ): Long? = readNumber(source, key)?.toLongOrNull()

        private fun readNumber(
            source: String,
            key: String,
        ): String? {
            val at = source.indexOf(key)
            if (at < 0) return null
            var end = at + key.length
            if (end < source.length && source[end] == '-') end++
            while (end < source.length && source[end].isDigit()) end++
            return source.substring(at + key.length, end)
        }
    }
}

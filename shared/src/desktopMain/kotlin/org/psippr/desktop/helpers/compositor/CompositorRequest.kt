package org.psippr.desktop.helpers.compositor

import org.psippr.desktop.log.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.Channels
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

/** One request to the psippr compositor's IPC socket and its one line answer. */
object CompositorRequest {
    /** The answer line to [request] (a JSON line), null when no compositor runs or it fails. */
    fun send(request: String): String? {
        val path = CompositorSocket.path() ?: return null
        return runCatching {
            SocketChannel.open(StandardProtocolFamily.UNIX).use { channel ->
                channel.connect(UnixDomainSocketAddress.of(path))
                val writer = Channels.newOutputStream(channel)
                writer.write((request + "\n").toByteArray(StandardCharsets.UTF_8))
                writer.flush()
                BufferedReader(InputStreamReader(Channels.newInputStream(channel), StandardCharsets.UTF_8)).readLine()
            }
        }.onFailure { e -> Log.w("CompositorRequest: $request failed: ${e.message}") }.getOrNull()
    }
}

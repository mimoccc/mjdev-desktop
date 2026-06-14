/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.helpers.mouseevents

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.mjdev.desktop.log.Log
import java.awt.Point
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Pointer source backed by the mjdev compositor's IPC socket.
 *
 * The desktop shell runs *inside* mjdevc (both nested and on a real session), and the
 * compositor knows the true cursor position — unlike AWT's global pointer, which is stuck at
 * screen-centre under nested XWayland. When the socket is present we subscribe and feed the
 * broadcast `{"event":"pointer","x":..,"y":..}` lines into the same edge-detection pipeline.
 * Each window opens its own connection, so every overlay window receives the global pointer.
 */
class CompositorMouseSource private constructor(
    private val channel: SocketChannel,
    private val scope: CoroutineScope,
    private val onEvent: (event: Point) -> Unit,
) {
    private var job: Job? = null
    private var pointerCount = 0L

    private fun start() {
        Log.d("CompositorMouseSource: subscribing to compositor pointer stream")
        channel.write(ByteBuffer.wrap("{\"cmd\":\"subscribe\"}\n".toByteArray(StandardCharsets.UTF_8)))
        job =
            scope.launch(Dispatchers.IO) {
                val buffer = ByteBuffer.allocate(8192)
                val pending = StringBuilder()
                runCatching {
                    while (isActive) {
                        buffer.clear()
                        val read = channel.read(buffer)
                        if (read < 0) break
                        if (read == 0) continue
                        buffer.flip()
                        pending.append(StandardCharsets.UTF_8.decode(buffer))
                        var nl = pending.indexOf("\n")
                        while (nl >= 0) {
                            handleLine(pending.substring(0, nl))
                            pending.delete(0, nl + 1)
                            nl = pending.indexOf("\n")
                        }
                    }
                }.onFailure { e -> Log.w("CompositorMouseSource: stream ended: ${e.message}") }
                Log.d("CompositorMouseSource: pointer stream closed")
            }
    }

    // line shape is fixed: {"event":"pointer","x":<int>,"y":<int>} — parse without a json dep
    private fun handleLine(line: String) {
        if (!line.contains("\"event\":\"pointer\"")) return
        val x = readInt(line, "\"x\":") ?: return
        val y = readInt(line, "\"y\":") ?: return
        // medium verbose: pointer is high frequency, sample 1/POINTER_SAMPLE
        if (pointerCount++ % POINTER_SAMPLE == 0L) {
            Log.d("CompositorMouseSource: pointer ($x, $y) [#$pointerCount]")
        }
        onEvent(Point(x, y))
    }

    private fun readInt(
        source: String,
        key: String,
    ): Int? {
        val at = source.indexOf(key)
        if (at < 0) return null
        var end = at + key.length
        if (end < source.length && source[end] == '-') end++
        while (end < source.length && source[end].isDigit()) end++
        return source.substring(at + key.length, end).toIntOrNull()
    }

    fun dispose() {
        Log.d("CompositorMouseSource: dispose")
        job?.cancel()
        runCatching { channel.close() }
    }

    companion object {
        // medium verbose: pointer is high frequency, only 1 in POINTER_SAMPLE is logged
        private const val POINTER_SAMPLE = 30L

        private fun socketPath(): Path? {
            val dir = System.getenv("XDG_RUNTIME_DIR") ?: return null
            val path = Path.of(dir, "mjdev-compositor.sock")
            return if (Files.exists(path)) path else null
        }

        /** Connects to a running compositor; returns null when none is running. */
        fun connect(
            scope: CoroutineScope,
            onEvent: (event: Point) -> Unit,
        ): CompositorMouseSource? {
            val path = socketPath()
            if (path == null) {
                Log.d("CompositorMouseSource: no compositor socket (XDG_RUNTIME_DIR/mjdev-compositor.sock) — not connecting")
                return null
            }
            return runCatching {
                val channel = SocketChannel.open(StandardProtocolFamily.UNIX)
                channel.connect(UnixDomainSocketAddress.of(path))
                Log.i("CompositorMouseSource: connected to compositor at $path")
                CompositorMouseSource(channel, scope, onEvent).also { it.start() }
            }.onFailure { e -> Log.w("CompositorMouseSource: connect failed: ${e.message}") }.getOrNull()
        }
    }
}

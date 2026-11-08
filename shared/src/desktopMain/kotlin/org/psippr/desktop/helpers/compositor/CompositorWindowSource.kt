package org.psippr.desktop.helpers.compositor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.psippr.desktop.helpers.mouseevents.CompositorPointerScale
import org.psippr.desktop.log.Log
import org.psippr.desktop.state.DesktopState
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.Channels
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

/**
 * Feeds the rectangles of the app windows on screen from the psippr compositor. The compositor
 * sends no event when a window moves or resizes, so the window list is asked for every
 * [POLL_INTERVAL_MS]; [onWindows] gets every list (AWT coordinates), also unchanged ones, so a
 * receiver that was not ready yet catches up on the next poll.
 */
class CompositorWindowSource private constructor(
    private val channel: SocketChannel,
    private val scope: CoroutineScope,
    private val onWindows: (windows: List<DesktopState.ScreenRect>) -> Unit,
) {
    private var job: Job? = null

    private fun start() {
        job =
            scope.launch(Dispatchers.IO) {
                val writer = Channels.newOutputStream(channel)
                val reader = BufferedReader(InputStreamReader(Channels.newInputStream(channel), StandardCharsets.UTF_8))
                runCatching {
                    while (isActive) {
                        writer.write((CompositorWindowList.REQUEST + "\n").toByteArray(StandardCharsets.UTF_8))
                        writer.flush()
                        val reply = reader.readLine() ?: break
                        val windows =
                            CompositorWindowList.appWindows(
                                reply,
                                CompositorPointerScale.scaleX(),
                                CompositorPointerScale.scaleY(),
                            )
                        onWindows(windows)
                        delay(POLL_INTERVAL_MS)
                    }
                }.onFailure { e -> Log.w("CompositorWindowSource: stream ended: ${e.message}") }
            }
    }

    fun dispose() {
        job?.cancel()
        runCatching { channel.close() }
    }

    companion object {
        /** Pause between two window list requests: quick enough for the bar, cheap for the socket. */
        private const val POLL_INTERVAL_MS = 300L

        /** Connects to a running compositor; null when none runs (plain JVM run: nothing overlaps). */
        fun connect(
            scope: CoroutineScope,
            onWindows: (windows: List<DesktopState.ScreenRect>) -> Unit,
        ): CompositorWindowSource? {
            val path = CompositorSocket.path() ?: return null
            return runCatching {
                val channel = SocketChannel.open(StandardProtocolFamily.UNIX)
                channel.connect(UnixDomainSocketAddress.of(path))
                Log.i("CompositorWindowSource: connected to compositor at $path")
                CompositorWindowSource(channel, scope, onWindows).also { source -> source.start() }
            }.onFailure { e -> Log.w("CompositorWindowSource: connect failed: ${e.message}") }.getOrNull()
        }
    }
}

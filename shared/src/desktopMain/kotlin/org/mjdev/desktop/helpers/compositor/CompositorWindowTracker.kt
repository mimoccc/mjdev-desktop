package org.mjdev.desktop.helpers.compositor

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.mjdev.desktop.log.Log
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Tracks non-shell app windows via mjdevc IPC so the dock can autohide only when an app
 * window actually overlaps the bottom bar (Windows-style intelligent hide).
 */
class CompositorWindowTracker(
    private val scope: CoroutineScope,
) {
    val appWindows: SnapshotStateList<CompositorAppWindow> = mutableStateListOf()
    private var job: Job? = null

    fun start() {
        if (job != null) return
        job =
            scope.launch(Dispatchers.IO) {
                runCatching { runLoop() }.onFailure { e ->
                    Log.w("CompositorWindowTracker: stopped: ${e.message}")
                }
            }
    }

    fun stop() {
        job?.cancel()
        job = null
        appWindows.clear()
    }

    fun isDockOccluded(
        screenWidth: Int,
        screenHeight: Int,
        dockZoneTopY: Int,
    ): Boolean = anyAppOccludesDockZone(appWindows, screenWidth, screenHeight, dockZoneTopY)

    private suspend fun runLoop() {
        val path = socketPath() ?: return
        SocketChannel.open(StandardProtocolFamily.UNIX).use { channel ->
            channel.connect(UnixDomainSocketAddress.of(path))
            channel.write(
                ByteBuffer.wrap("{\"cmd\":\"subscribe\"}\n".toByteArray(StandardCharsets.UTF_8)),
            )
            Log.d("CompositorWindowTracker: connected, subscribed")
            requestList(channel)
            val pollJob =
                scope.launch(Dispatchers.IO) {
                    while (isActive) {
                        delay(POLL_MS)
                        runCatching { requestList(channel) }
                    }
                }
            try {
                readLines(channel) { line -> handleLine(line) }
            } finally {
                pollJob.cancel()
            }
        }
    }

    private fun requestList(channel: SocketChannel) {
        channel.write(
            ByteBuffer.wrap("{\"cmd\":\"list-windows\"}\n".toByteArray(StandardCharsets.UTF_8)),
        )
    }

    private fun handleLine(line: String) {
        val root = runCatching { JsonParser.parseString(line).asJsonObject }.getOrNull() ?: return
        when {
            root.has("windows") -> applyList(root.getAsJsonArray("windows"))
            root.get("event")?.asString == "window-opened" -> {
                root.getAsJsonObject("window")?.let { obj ->
                    parseWindow(obj)?.let { upsert(it) }
                }
            }
            root.get("event")?.asString == "window-closed" -> {
                val id = root.getAsJsonObject("window")?.get("id")?.asLong ?: return
                appWindows.removeAll { it.id == id }
            }
        }
    }

    private fun applyList(array: JsonArray) {
        val next = buildList {
            for (i in 0 until array.size()) {
                parseWindow(array[i].asJsonObject)?.let { add(it) }
            }
        }
        appWindows.clear()
        appWindows.addAll(next)
    }

    private fun upsert(window: CompositorAppWindow) {
        val idx = appWindows.indexOfFirst { it.id == window.id }
        if (idx >= 0) {
            appWindows[idx] = window
        } else {
            appWindows.add(window)
        }
    }

    private fun parseWindow(obj: JsonObject): CompositorAppWindow? {
        val id = obj.get("id")?.asLong ?: return null
        return CompositorAppWindow(
            id = id,
            x = obj.get("x")?.asInt ?: 0,
            y = obj.get("y")?.asInt ?: 0,
            width = obj.get("width")?.asInt ?: 0,
            height = obj.get("height")?.asInt ?: 0,
            minimized = obj.get("minimized")?.asBoolean == true,
        )
    }

    private fun readLines(
        channel: SocketChannel,
        onLine: (String) -> Unit,
    ) {
        val buffer = ByteBuffer.allocate(8192)
        val pending = StringBuilder()
        while (true) {
            buffer.clear()
            val read = channel.read(buffer)
            if (read < 0) break
            if (read == 0) continue
            buffer.flip()
            pending.append(StandardCharsets.UTF_8.decode(buffer))
            var nl = pending.indexOf("\n")
            while (nl >= 0) {
                onLine(pending.substring(0, nl))
                pending.delete(0, nl + 1)
                nl = pending.indexOf("\n")
            }
        }
    }

    companion object {
        private const val POLL_MS = 400L

        private fun socketPath(): Path? {
            val dir = System.getenv("XDG_RUNTIME_DIR") ?: return null
            val path = Path.of(dir, "mjdev-compositor.sock")
            return if (Files.exists(path)) path else null
        }
    }
}

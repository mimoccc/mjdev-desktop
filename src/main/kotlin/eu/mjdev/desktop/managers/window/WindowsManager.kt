/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.window

import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import com.google.gson.JsonObject
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.log.Log
import eu.mjdev.desktop.provider.DesktopProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.Channels
import java.nio.channels.SocketChannel

/**
 * Client of the mjdev compositor IPC api
 * ($XDG_RUNTIME_DIR/mjdev-compositor.sock).
 * When the desktop does not run inside the mjdev compositor
 * (no socket), every query falls back to empty results.
 */
@Suppress("unused")
class WindowsManager(
    val api: DesktopProvider,
    val scope: CoroutineScope = api.scope,
) {
    private val gson = Gson()
    private val socketPath: String? = sequenceOf(
        System.getenv("MJDEV_COMPOSITOR_SOCKET"),
        System.getenv("XDG_RUNTIME_DIR")?.let { "$it/mjdev-compositor.sock" }
    ).filterNotNull().firstOrNull { File(it).exists() }

    val isCompositorAvailable: Boolean
        get() = socketPath != null

    private val windowsState = mutableStateOf<List<SystemWindow>>(emptyList())
    private var eventsJob: Job? = null

    val windows: List<SystemWindow>
        get() = windowsState.value

    init {
        if (isCompositorAvailable) {
            startEventLoop()
        }
    }

    fun dispose() {
        eventsJob?.cancel()
        eventsJob = null
    }

    fun findWindows(app: App? = null): List<SystemWindow> =
        if (app == null) windows else windows.filter { it.matches(app) }

    fun hasWindow(app: App): Boolean =
        findWindows(app).isNotEmpty()

    fun isWindowFocus(app: App): Boolean =
        findWindows(app).any { it.focused }

    fun requestWindowsFocus(app: App): Boolean =
        commandForAll(app, "activate")

    fun minimizeWindows(app: App): Boolean =
        commandForAll(app, "minimize")

    fun closeWindows(app: App): Boolean =
        commandForAll(app, "close")

    fun activateWindow(window: SystemWindow) {
        request(mapOf("cmd" to "activate", "id" to window.id))
    }

    fun minimizeWindow(window: SystemWindow) {
        request(mapOf("cmd" to "minimize", "id" to window.id))
    }

    fun closeWindow(window: SystemWindow) {
        request(mapOf("cmd" to "close", "id" to window.id))
    }

    /** windows matching given app, exposed for taskbar style uses */
    fun windowsOf(app: App): List<SystemWindow> = findWindows(app)

    // ------------------------------------------------------------------

    private fun SystemWindow.matches(app: App): Boolean =
        windowClass.isNotEmpty() && (
                windowClass.contains(app.windowClass, true) ||
                        windowClass.contains(app.fullAppName, true) ||
                        title.contains(app.fullAppName, true)
                )

    private fun commandForAll(app: App, cmd: String): Boolean {
        val targets = findWindows(app)
        targets.forEach { window ->
            request(mapOf("cmd" to cmd, "id" to window.id))
        }
        return targets.isNotEmpty()
    }

    private fun request(payload: Map<String, Any?>): JsonObject? {
        val path = socketPath ?: return null
        return runCatching {
            SocketChannel.open(StandardProtocolFamily.UNIX).use { channel ->
                channel.connect(UnixDomainSocketAddress.of(path))
                val output = Channels.newOutputStream(channel)
                output.write((gson.toJson(payload) + "\n").toByteArray())
                output.flush()
                Channels.newInputStream(channel).bufferedReader().readLine()
                    ?.let { gson.fromJson(it, JsonObject::class.java) }
            }
        }.onFailure { error ->
            Log.e("Compositor ipc request failed: ${error.message}")
        }.getOrNull()
    }

    private fun refreshWindows() {
        val response = request(mapOf("cmd" to "list-windows")) ?: return
        if (response.get("ok")?.asBoolean != true) return
        windowsState.value = response.getAsJsonArray("windows").mapNotNull { element ->
            runCatching { element.asJsonObject.toSystemWindow() }.getOrNull()
        }
    }

    private fun JsonObject.toSystemWindow() = SystemWindow(
        id = get("id").asLong,
        pid = get("pid")?.asLong ?: 0L,
        windowClass = get("app_id")?.takeIf { !it.isJsonNull }?.asString.orEmpty(),
        desktop = 0L,
        command = "",
        title = get("title")?.takeIf { !it.isJsonNull }?.asString.orEmpty(),
        minimized = get("minimized")?.asBoolean ?: false,
        focused = get("focused")?.asBoolean ?: false,
    )

    private fun startEventLoop() {
        val path = socketPath ?: return
        eventsJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                runCatching {
                    SocketChannel.open(StandardProtocolFamily.UNIX).use { channel ->
                        channel.connect(UnixDomainSocketAddress.of(path))
                        val output = Channels.newOutputStream(channel)
                        output.write("{\"cmd\":\"subscribe\"}\n".toByteArray())
                        output.flush()
                        refreshWindows()
                        val reader = Channels.newInputStream(channel).bufferedReader()
                        while (isActive) {
                            reader.readLine() ?: break
                            // any window event invalidates the list
                            refreshWindows()
                        }
                    }
                }.onFailure { error ->
                    Log.e("Compositor ipc events failed: ${error.message}")
                }
                if (isActive) {
                    delay(RECONNECT_DELAY)
                }
            }
        }
    }

    data class SystemWindow(
        val id: Long,
        val pid: Long,
        val windowClass: String,
        val desktop: Long,
        val command: String,
        val title: String = "",
        val minimized: Boolean = false,
        val focused: Boolean = false,
    )

    companion object {
        private const val RECONNECT_DELAY = 3000L
    }
}

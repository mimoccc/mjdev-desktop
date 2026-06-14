/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

@file:OptIn(ExperimentalForeignApi::class)

package eu.mjdev.compositor

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import mjdev.compositor.shim.MJC_EVENT_ERROR
import mjdev.compositor.shim.MJC_EVENT_HANGUP
import mjdev.compositor.shim.MJC_EVENT_READABLE
import mjdev.compositor.shim.mjc_loop_add_fd
import mjdev.compositor.shim.mjc_loop_remove_fd
import mjdev.compositor.shim.mjc_view_focus
import mjdev.compositor.shim.mjc_view_close
import mjdev.compositor.shim.mjc_view_is_maximized
import mjdev.compositor.shim.mjc_view_set_maximized
import mjdev.compositor.shim.mjc_view_set_minimized
import mjdev.compositor.shim.mjc_unix_accept
import mjdev.compositor.shim.mjc_unix_listen
import platform.posix.EAGAIN
import platform.posix.EWOULDBLOCK
import platform.posix.close
import platform.posix.errno
import platform.posix.read
import platform.posix.unlink
import platform.posix.write

/**
 * Line based JSON api on a unix socket.
 *
 * Requests:
 *   {"cmd":"list-windows"}
 *   {"cmd":"windows-for-app","app_id":"firefox"}
 *   {"cmd":"activate","id":3}
 *   {"cmd":"close","id":3}
 *   {"cmd":"minimize","id":3,"minimized":true}
 *   {"cmd":"maximize","id":3}
 *   {"cmd":"subscribe"}
 *
 * Events (only for subscribed clients):
 *   {"event":"window-opened","window":{...}}
 *   {"event":"window-closed","window":{...}}
 *   {"event":"window-title","window":{...}}
 *   {"event":"focus-changed","window":{...}|null}
 */
class IpcServer(private val c: Compositor) {

    private class Client(val fd: Int) {
        val buffer = StringBuilder()
        var subscribed = false
    }

    private val json = Json { ignoreUnknownKeys = true }
    private var serverFd = -1
    private val clients = mutableMapOf<Int, Client>()

    fun start() {
        val path = c.config.socketPath
        serverFd = mjc_unix_listen(path, 16)
        if (serverFd < 0) {
            println("mjdevc: cannot bind ipc socket $path")
            return
        }
        mjc_loop_add_fd(c.server, serverFd, MJC_EVENT_READABLE.convert())
        println("mjdevc: ipc listening on $path")
    }

    fun stop() {
        clients.keys.toList().forEach { dropClient(it) }
        if (serverFd >= 0) {
            mjc_loop_remove_fd(c.server, serverFd)
            close(serverFd)
            unlink(c.config.socketPath)
            serverFd = -1
        }
    }

    fun handleFd(fd: Int, mask: UInt): Int {
        if (fd == serverFd) {
            acceptClients()
            return 0
        }
        val errorMask = (MJC_EVENT_HANGUP or MJC_EVENT_ERROR).convert<UInt>()
        if (mask and errorMask != 0u) {
            dropClient(fd)
            return 0
        }
        readClient(fd)
        return 0
    }

    private fun acceptClients() {
        while (true) {
            val fd = mjc_unix_accept(serverFd)
            if (fd < 0) {
                return
            }
            clients[fd] = Client(fd)
            Clog.v("ipc client connected fd=$fd (clients=${clients.size})")
            mjc_loop_add_fd(
                c.server, fd,
                (MJC_EVENT_READABLE or MJC_EVENT_HANGUP or MJC_EVENT_ERROR).convert()
            )
        }
    }

    private fun readClient(fd: Int) {
        val client = clients[fd] ?: return
        val chunk = ByteArray(4096)
        while (true) {
            val n = chunk.usePinned { pinned ->
                read(fd, pinned.addressOf(0), chunk.size.convert()).toInt()
            }
            when {
                n > 0 -> client.buffer.append(chunk.decodeToString(0, n))
                n == 0 -> {
                    dropClient(fd)
                    return
                }

                else -> {
                    if (errno != EAGAIN && errno != EWOULDBLOCK) {
                        dropClient(fd)
                    }
                    break
                }
            }
        }
        processBuffer(client)
    }

    private fun processBuffer(client: Client) {
        while (true) {
            val text = client.buffer.toString()
            val newline = text.indexOf('\n')
            if (newline < 0) {
                return
            }
            val line = text.substring(0, newline).trim()
            client.buffer.deleteRange(0, newline + 1)
            if (line.isNotEmpty()) {
                val response = runCatching { handleRequest(client, line) }
                    .getOrElse { error ->
                        buildJsonObject {
                            put("ok", false)
                            put("error", error.message ?: "invalid request")
                        }
                    }
                send(client.fd, response)
            }
        }
    }

    private fun handleRequest(client: Client, line: String): JsonObject {
        val request = json.parseToJsonElement(line).jsonObject
        val cmd = request["cmd"]?.jsonPrimitive?.content
            ?: return error("missing cmd")
        Clog.v("ipc <- fd=${client.fd}: $line")
        return when (cmd) {
            "list-windows" -> {
                val all = request["all"]?.jsonPrimitive?.content?.toBoolean() == true
                val windows = if (all) {
                    c.windows.all().filter { it.mapped }
                } else {
                    c.windows.listed()
                }
                ok { put("windows", JsonArray(windows.map { it.toJson() })) }
            }

            "windows-for-app" -> {
                val appId = request["app_id"]?.jsonPrimitive?.content
                    ?: return error("missing app_id")
                ok {
                    put("windows", JsonArray(
                        c.windows.listed().filter {
                            it.appId?.contains(appId, ignoreCase = true) == true
                        }.map { it.toJson() }
                    ))
                }
            }

            "activate" -> withWindow(request) { info ->
                if (info.minimized) {
                    mjc_view_set_minimized(info.ptr, false)
                    info.minimized = false
                }
                mjc_view_focus(info.ptr)
            }

            "close" -> withWindow(request) { mjc_view_close(it.ptr) }

            "minimize" -> withWindow(request) { info ->
                val minimized = request["minimized"]
                    ?.jsonPrimitive?.content?.toBoolean() ?: true
                mjc_view_set_minimized(info.ptr, minimized)
                info.minimized = minimized
            }

            "maximize" -> withWindow(request) { info ->
                val maximized = request["maximized"]
                    ?.jsonPrimitive?.content?.toBoolean()
                    ?: !mjc_view_is_maximized(info.ptr)
                mjc_view_set_maximized(info.ptr, maximized)
            }

            "subscribe" -> {
                client.subscribed = true
                ok { }
            }

            else -> error("unknown cmd $cmd")
        }
    }

    private inline fun withWindow(
        request: JsonObject,
        block: (WindowInfo) -> Unit
    ): JsonObject {
        val id = request["id"]?.jsonPrimitive?.long ?: return error("missing id")
        val info = c.windows.byId(id) ?: return error("no window $id")
        block(info)
        return ok { }
    }

    private inline fun ok(block: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit) =
        buildJsonObject {
            put("ok", true)
            block()
        }

    private fun error(message: String) = buildJsonObject {
        put("ok", false)
        put("error", message)
    }

    fun broadcastEvent(event: String, info: WindowInfo?) {
        val subscribers = clients.values.count { it.subscribed }
        if (subscribers == 0) {
            return
        }
        Clog.v("ipc -> $event to $subscribers subscriber(s): ${info?.describe() ?: "null"}")
        val payload = buildJsonObject {
            put("event", event)
            put("window", info?.toJson() ?: JsonNull)
        }
        clients.values.filter { it.subscribed }.forEach { send(it.fd, payload) }
    }

    fun broadcastPointer(x: Int, y: Int) {
        if (clients.values.none { it.subscribed }) {
            return
        }
        val payload = buildJsonObject {
            put("event", "pointer")
            put("x", x)
            put("y", y)
        }
        clients.values.filter { it.subscribed }.forEach { send(it.fd, payload) }
    }

    private fun send(fd: Int, payload: JsonObject) {
        val data = (payload.toString() + "\n").encodeToByteArray()
        data.usePinned { pinned ->
            var offset = 0
            while (offset < data.size) {
                val n = write(fd, pinned.addressOf(offset), (data.size - offset).convert()).toInt()
                if (n <= 0) {
                    break
                }
                offset += n
            }
        }
    }

    private fun dropClient(fd: Int) {
        if (clients.remove(fd) != null) {
            Clog.v("ipc client disconnected fd=$fd (clients=${clients.size})")
            mjc_loop_remove_fd(c.server, fd)
            close(fd)
        }
    }
}

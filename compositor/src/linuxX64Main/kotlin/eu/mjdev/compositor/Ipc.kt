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
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import mjdev.compositor.shim.MJC_EVENT_ERROR
import mjdev.compositor.shim.MJC_EVENT_HANGUP
import mjdev.compositor.shim.MJC_EVENT_READABLE
import mjdev.compositor.shim.MJC_LAYER_BOTTOM
import mjdev.compositor.shim.MJC_LAYER_NORMAL
import mjdev.compositor.shim.MJC_LAYER_TOP
import mjdev.compositor.shim.mjc_loop_add_fd
import mjdev.compositor.shim.mjc_loop_remove_fd
import mjdev.compositor.shim.mjc_key
import mjdev.compositor.shim.mjc_pointer_button
import mjdev.compositor.shim.mjc_pointer_move
import mjdev.compositor.shim.mjc_set_decoration_theme
import mjdev.compositor.shim.mjc_view_focus
import mjdev.compositor.shim.mjc_view_close
import mjdev.compositor.shim.mjc_view_is_maximized
import mjdev.compositor.shim.mjc_view_set_decorated
import mjdev.compositor.shim.mjc_view_set_layer
import mjdev.compositor.shim.mjc_view_set_maximized
import mjdev.compositor.shim.mjc_view_set_minimized
import mjdev.compositor.shim.mjc_view_set_position
import mjdev.compositor.shim.mjc_unix_accept
import mjdev.compositor.shim.mjc_unix_listen
import platform.posix.EAGAIN
import platform.posix.EWOULDBLOCK
import platform.posix.close
import platform.posix.errno
import platform.posix.read
import platform.posix.unlink
import platform.posix.write

/** evdev code for the left mouse button (linux input-event-codes.h BTN_LEFT) */
private const val BTN_LEFT = 0x110

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
 *   {"cmd":"pointer-move","x":100,"y":200}
 *   {"cmd":"button","button":272,"pressed":true}   // 272 = BTN_LEFT; omit button for left
 *   {"cmd":"click","x":100,"y":200}                // optional x/y, then press+release
 *   {"cmd":"key","code":1,"pressed":true}          // evdev code; omit pressed to tap
 *   {"cmd":"move","id":1,"x":100,"y":100}          // move a window to absolute position
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
                    // process any complete line received right before the peer half-closed
                    // (a `printf ... | nc -U` client sends data then EOF in one go), otherwise
                    // the request is silently dropped
                    processBuffer(client)
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

            // input injection for automated/headless testing (drives the seat directly)
            "pointer-move" -> {
                val x = request["x"]?.jsonPrimitive?.int ?: return error("missing x")
                val y = request["y"]?.jsonPrimitive?.int ?: return error("missing y")
                mjc_pointer_move(c.server, x, y)
                ok { }
            }

            "button" -> {
                val button = request["button"]?.jsonPrimitive?.int ?: BTN_LEFT
                val pressed = request["pressed"]?.jsonPrimitive?.content?.toBoolean() ?: true
                mjc_pointer_button(c.server, button.toUInt(), pressed)
                ok { }
            }

            "click" -> {
                val button = request["button"]?.jsonPrimitive?.int ?: BTN_LEFT
                val x = request["x"]?.jsonPrimitive?.int
                val y = request["y"]?.jsonPrimitive?.int
                if (x != null && y != null) {
                    mjc_pointer_move(c.server, x, y)
                }
                mjc_pointer_button(c.server, button.toUInt(), true)
                mjc_pointer_button(c.server, button.toUInt(), false)
                ok { }
            }

            "key" -> {
                val code = request["code"]?.jsonPrimitive?.int ?: return error("missing code")
                val pressed = request["pressed"]?.jsonPrimitive?.content?.toBoolean()
                if (pressed == null) {
                    // no explicit state -> tap (press + release)
                    mjc_key(c.server, code.toUInt(), true)
                    mjc_key(c.server, code.toUInt(), false)
                } else {
                    mjc_key(c.server, code.toUInt(), pressed)
                }
                ok { }
            }

            // move a window directly (deterministic; injected drag can't start an
            // interactive move because the client's move request needs a real grab serial)
            "move" -> {
                val id = request["id"]?.jsonPrimitive?.long ?: return error("missing id")
                val x = request["x"]?.jsonPrimitive?.int ?: return error("missing x")
                val y = request["y"]?.jsonPrimitive?.int ?: return error("missing y")
                val info = c.windows.byId(id) ?: return error("no window $id")
                mjc_view_set_position(info.ptr, x, y)
                ok { }
            }

            // server-side decoration frame color, rgb channels 0..255; fg is optional
            // and defaults to a contrasting black/white based on bg luminance
            "set-decoration-theme" -> {
                val br = request["bg_r"]?.jsonPrimitive?.int ?: return error("missing bg_r")
                val bgc = request["bg_g"]?.jsonPrimitive?.int ?: return error("missing bg_g")
                val bb = request["bg_b"]?.jsonPrimitive?.int ?: return error("missing bg_b")
                val lum = (0.2126 * br + 0.7152 * bgc + 0.0722 * bb) / 255.0
                val defFg = if (lum > 0.5) 0 else 255
                val fr = request["fg_r"]?.jsonPrimitive?.int ?: defFg
                val fgc = request["fg_g"]?.jsonPrimitive?.int ?: defFg
                val fb = request["fg_b"]?.jsonPrimitive?.int ?: defFg
                val ibr = request["icon_bg_r"]?.jsonPrimitive?.int ?: br
                val ibgc = request["icon_bg_g"]?.jsonPrimitive?.int ?: bgc
                val ibb = request["icon_bg_b"]?.jsonPrimitive?.int ?: bb
                val ifr = request["icon_fg_r"]?.jsonPrimitive?.int ?: fr
                val ifgc = request["icon_fg_g"]?.jsonPrimitive?.int ?: fgc
                val ifb = request["icon_fg_b"]?.jsonPrimitive?.int ?: fb
                mjc_set_decoration_theme(
                    c.server,
                    br / 255f, bgc / 255f, bb / 255f,
                    fr / 255f, fgc / 255f, fb / 255f,
                    ibr / 255f, ibgc / 255f, ibb / 255f,
                    ifr / 255f, ifgc / 255f, ifb / 255f,
                )
                ok { }
            }

            // chromeless / frameless toggle for a specific window
            "set-decorated" -> withWindow(request) { info ->
                val decorated = request["decorated"]
                    ?.jsonPrimitive?.content?.toBoolean() ?: true
                mjc_view_set_decorated(info.ptr, decorated)
            }

            // window stacking: always-on-top / always-on-bottom (apps keep their frame)
            "always-on-top" -> withWindow(request) { info ->
                val on = request["on"]?.jsonPrimitive?.content?.toBoolean() ?: true
                mjc_view_set_layer(info.ptr, if (on) MJC_LAYER_TOP else MJC_LAYER_NORMAL)
            }

            "always-on-bottom" -> withWindow(request) { info ->
                val on = request["on"]?.jsonPrimitive?.content?.toBoolean() ?: true
                mjc_view_set_layer(info.ptr, if (on) MJC_LAYER_BOTTOM else MJC_LAYER_NORMAL)
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

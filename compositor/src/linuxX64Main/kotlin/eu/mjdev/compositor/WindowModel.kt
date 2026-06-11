/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

@file:OptIn(ExperimentalForeignApi::class)

package eu.mjdev.compositor

import cnames.structs.mjc_view
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mjdev.compositor.shim.mjc_view_app_id
import mjdev.compositor.shim.mjc_view_get_geometry
import mjdev.compositor.shim.mjc_view_id
import mjdev.compositor.shim.mjc_view_is_maximized
import mjdev.compositor.shim.mjc_view_pid
import mjdev.compositor.shim.mjc_view_title

class WindowInfo(
    val ptr: CPointer<mjc_view>,
    val id: ULong,
    val xwayland: Boolean,
) {
    var appId: String? = null
    var title: String? = null
    var pid: Int = 0
    var mapped: Boolean = false
    var minimized: Boolean = false
    var focused: Boolean = false
    var shell: Boolean = false
    var role: String? = null

    /* pulls current strings/pid from the native view */
    fun refresh() {
        appId = mjc_view_app_id(ptr)?.toKString() ?: appId
        title = mjc_view_title(ptr)?.toKString() ?: title
        pid = mjc_view_pid(ptr)
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("id", id.toLong())
        put("app_id", appId)
        put("title", title)
        put("pid", pid)
        put("xwayland", xwayland)
        put("mapped", mapped)
        put("minimized", minimized)
        put("maximized", mjc_view_is_maximized(ptr))
        memScoped {
            val x = alloc<IntVar>()
            val y = alloc<IntVar>()
            val w = alloc<IntVar>()
            val h = alloc<IntVar>()
            mjc_view_get_geometry(ptr, x.ptr, y.ptr, w.ptr, h.ptr)
            put("x", x.value)
            put("y", y.value)
            put("width", w.value)
            put("height", h.value)
        }
        put("focused", focused)
        put("shell", shell)
    }
}

class WindowModel {
    private val byPtr = LinkedHashMap<Long, WindowInfo>()

    fun add(view: CPointer<mjc_view>, xwayland: Boolean): WindowInfo {
        val info = WindowInfo(view, mjc_view_id(view), xwayland)
        byPtr[view.rawValue.toLong()] = info
        return info
    }

    fun get(view: CPointer<mjc_view>): WindowInfo? =
        byPtr[view.rawValue.toLong()]

    fun remove(view: CPointer<mjc_view>): WindowInfo? =
        byPtr.remove(view.rawValue.toLong())

    fun byId(id: Long): WindowInfo? =
        byPtr.values.firstOrNull { it.id.toLong() == id }

    fun all(): List<WindowInfo> = byPtr.values.toList()

    /* windows visible to clients of the IPC api (no shell internals) */
    fun listed(): List<WindowInfo> =
        byPtr.values.filter { it.mapped && !it.shell }
}

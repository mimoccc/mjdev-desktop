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
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import mjdev.compositor.shim.mjc_view_get_floating_geometry
import mjdev.compositor.shim.mjc_view_is_maximized
import mjdev.compositor.shim.mjc_view_set_geometry
import mjdev.compositor.shim.mjc_view_set_maximized
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.getenv
import platform.posix.mkdir

/**
 * Remembers the last user set geometry of app windows (keyed by app id)
 * across compositor restarts, so windows reopen where the user left them.
 */
class GeometryStore {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer =
        MapSerializer(String.serializer(), ListSerializer(Int.serializer()))
    private val entries = mutableMapOf<String, List<Int>>()
    private val path: String

    init {
        val home = getenv("HOME")?.toKString() ?: "/tmp"
        val stateBase = getenv("XDG_STATE_HOME")?.toKString()
            ?: "$home/.local/state"
        val dir = "$stateBase/mjdev"
        mkdir(stateBase, DIR_MODE)
        mkdir(dir, DIR_MODE)
        path = "$dir/window-geometry.json"
        load()
    }

    /**
     * applies the remembered state to a freshly mapped window: the floating
     * geometry first (it doubles as the restore target of a later
     * unmaximize) and the remembered maximized flag on top; the last state
     * the user left the app in wins over whatever the client requested
     */
    fun restore(info: WindowInfo) {
        val geo = entries[keyOf(info) ?: return] ?: return
        if (geo.size >= 4) {
            mjc_view_set_geometry(info.ptr, geo[0], geo[1], geo[2], geo[3])
        }
        if (geo.size >= 5 && geo[4] == 1) {
            mjc_view_set_maximized(info.ptr, true)
        }
    }

    /** captures the state of a window that is going away */
    fun remember(info: WindowInfo) {
        val key = keyOf(info) ?: return
        val maximized = mjc_view_is_maximized(info.ptr)
        memScoped {
            val x = alloc<IntVar>()
            val y = alloc<IntVar>()
            val w = alloc<IntVar>()
            val h = alloc<IntVar>()
            // floating geometry, for maximized windows the restore target
            mjc_view_get_floating_geometry(info.ptr, x.ptr, y.ptr, w.ptr, h.ptr)
            if (w.value <= 0 || h.value <= 0) {
                return
            }
            val geo = listOf(x.value, y.value, w.value, h.value, if (maximized) 1 else 0)
            if (entries[key] != geo) {
                entries[key] = geo
                save()
            }
        }
    }

    private fun keyOf(info: WindowInfo): String? =
        info.appId?.lowercase()?.takeIf { it.isNotBlank() }

    private fun load() {
        val file = fopen(path, "r") ?: return
        try {
            val content = buildString {
                memScoped {
                    val buffer = allocArray<ByteVar>(READ_CHUNK)
                    while (true) {
                        val line = fgets(buffer, READ_CHUNK, file) ?: break
                        append(line.toKString())
                    }
                }
            }
            if (content.isNotBlank()) {
                entries.putAll(json.decodeFromString(serializer, content))
            }
        } catch (_: Exception) {
            // corrupted store starts over, windows just lose their positions
            entries.clear()
        } finally {
            fclose(file)
        }
    }

    private fun save() {
        val file = fopen(path, "w") ?: return
        fputs(json.encodeToString(serializer, entries), file)
        fclose(file)
    }

    companion object {
        private const val READ_CHUNK = 4096
        private val DIR_MODE = 493u // 0755
    }
}

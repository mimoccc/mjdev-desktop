/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

@file:OptIn(ExperimentalForeignApi::class)

package eu.psippr.compositor

import cnames.structs.mjc_server
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import psippr.compositor.shim.mjc_output_current_mode
import psippr.compositor.shim.mjc_output_mode
import psippr.compositor.shim.mjc_output_mode_count
import psippr.compositor.shim.mjc_output_set_mode

/**
 * Screen modes of the main output for the IPC api (`list-modes`, `set-mode`), so the shell's
 * display settings can show and switch the resolution.
 */
object OutputModes {
    /** JSON field names of one mode. */
    const val KEY_WIDTH = "width"
    const val KEY_HEIGHT = "height"
    const val KEY_REFRESH = "refresh"
    const val KEY_CURRENT = "current"
    const val KEY_MODES = "modes"

    /** `{"current":{…},"modes":[…]}`; modes is empty on nested backends (any size works there). */
    fun describe(server: CPointer<mjc_server>): JsonObject = buildJsonObject {
        current(server)?.let { mode -> put(KEY_CURRENT, mode) }
        put(KEY_MODES, JsonArray(listed(server)))
    }

    /** Switches the main output; false when no such mode exists or the output refuses it. */
    fun set(
        server: CPointer<mjc_server>,
        width: Int,
        height: Int,
        refreshMilliHz: Int,
    ): Boolean = mjc_output_set_mode(server, width, height, refreshMilliHz)

    private fun current(server: CPointer<mjc_server>): JsonObject? = memScoped {
        val width = alloc<IntVar>()
        val height = alloc<IntVar>()
        val refresh = alloc<IntVar>()
        if (mjc_output_current_mode(server, width.ptr, height.ptr, refresh.ptr)) {
            mode(width.value, height.value, refresh.value)
        } else {
            null
        }
    }

    // distinct: some outputs list the same mode twice (e.g. with different flags)
    private fun listed(server: CPointer<mjc_server>): List<JsonObject> = (0 until mjc_output_mode_count(server))
        .mapNotNull { index ->
            memScoped {
                val width = alloc<IntVar>()
                val height = alloc<IntVar>()
                val refresh = alloc<IntVar>()
                if (mjc_output_mode(server, index, width.ptr, height.ptr, refresh.ptr)) {
                    mode(width.value, height.value, refresh.value)
                } else {
                    null
                }
            }
        }.distinct()

    private fun mode(
        width: Int,
        height: Int,
        refreshMilliHz: Int,
    ): JsonObject = buildJsonObject {
        put(KEY_WIDTH, width)
        put(KEY_HEIGHT, height)
        put(KEY_REFRESH, refreshMilliHz)
    }
}

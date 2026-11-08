package org.psippr.desktop.managers.display

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.psippr.desktop.helpers.compositor.CompositorRequest

/**
 * Screen mode of the main output under the psippr compositor (IPC `list-modes` / `set-mode`).
 * Nested runs list no modes: the host window takes any size, so there is nothing to choose from.
 */
class CompositorDisplayBackend : IDisplayBackend {
    override fun currentMode(): DisplayMode? = answer(LIST_MODES)?.get(KEY_CURRENT)?.jsonObject?.let(::mode)

    override fun modes(): List<DisplayMode> = answer(LIST_MODES)
        ?.get(KEY_MODES)
        ?.jsonArray
        ?.mapNotNull { element -> mode(element.jsonObject) }
        .orEmpty()

    override fun setMode(mode: DisplayMode): Result<Boolean> {
        val request =
            buildJsonObject {
                put(KEY_CMD, SET_MODE)
                put(KEY_WIDTH, mode.width)
                put(KEY_HEIGHT, mode.height)
                put(KEY_REFRESH, mode.refreshMilliHz)
            }
        val ok = answer(request.toString())?.get(KEY_OK)?.jsonPrimitive?.booleanOrNull ?: false
        return Result.success(ok)
    }

    // the answer object, null when no compositor runs or it answered with an error
    private fun answer(request: String): JsonObject? = CompositorRequest
        .send(request)
        ?.let { line -> runCatching { Json.parseToJsonElement(line).jsonObject }.getOrNull() }

    private fun mode(json: JsonObject): DisplayMode? {
        val width = json[KEY_WIDTH]?.jsonPrimitive?.intOrNull ?: return null
        val height = json[KEY_HEIGHT]?.jsonPrimitive?.intOrNull ?: return null
        return DisplayMode(width, height, json[KEY_REFRESH]?.jsonPrimitive?.intOrNull ?: 0)
    }

    private companion object {
        const val LIST_MODES = "{\"cmd\":\"list-modes\"}"
        const val SET_MODE = "set-mode"
        const val KEY_CMD = "cmd"
        const val KEY_OK = "ok"
        const val KEY_CURRENT = "current"
        const val KEY_MODES = "modes"
        const val KEY_WIDTH = "width"
        const val KEY_HEIGHT = "height"
        const val KEY_REFRESH = "refresh"
    }
}

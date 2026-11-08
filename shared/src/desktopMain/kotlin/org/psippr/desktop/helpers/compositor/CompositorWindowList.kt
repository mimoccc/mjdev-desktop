package org.psippr.desktop.helpers.compositor

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.psippr.desktop.helpers.mouseevents.CompositorPointerScale
import org.psippr.desktop.state.DesktopState

/**
 * Reads the compositor's `list-windows` reply: the rectangles of the app windows on screen
 * (mapped, not minimized, not part of the shell), in AWT screen coordinates like every other
 * rectangle of the shell.
 */
object CompositorWindowList {
    /** Request line asking for all windows. */
    const val REQUEST = "{\"cmd\":\"list-windows\"}"

    /** Reply field holding the window array. */
    private const val KEY_WINDOWS = "windows"
    private const val KEY_MAPPED = "mapped"
    private const val KEY_MINIMIZED = "minimized"
    private const val KEY_SHELL = "shell"
    private const val KEY_X = "x"
    private const val KEY_Y = "y"
    private const val KEY_WIDTH = "width"
    private const val KEY_HEIGHT = "height"

    /** App window rectangles of [reply] for the AWT scale [scaleX], [scaleY]; empty when unreadable. */
    fun appWindows(
        reply: String,
        scaleX: Double,
        scaleY: Double,
    ): List<DesktopState.ScreenRect> = runCatching {
        Json
            .parseToJsonElement(reply)
            .jsonObject[KEY_WINDOWS]
            ?.jsonArray
            ?.map { element -> element.jsonObject }
            ?.filter { window -> window.isOnScreenApp() }
            ?.mapNotNull { window -> window.rect(scaleX, scaleY) }
            .orEmpty()
    }.getOrDefault(emptyList())

    private fun JsonObject.flag(key: String): Boolean = this[key]?.jsonPrimitive?.booleanOrNull ?: false

    private fun JsonObject.number(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull

    private fun JsonObject.isOnScreenApp(): Boolean = flag(KEY_MAPPED) && !flag(KEY_MINIMIZED) && !flag(KEY_SHELL)

    // compositor pixels -> AWT units, through the same conversion as the pointer
    private fun JsonObject.rect(
        scaleX: Double,
        scaleY: Double,
    ): DesktopState.ScreenRect? {
        val x = number(KEY_X) ?: return null
        val y = number(KEY_Y) ?: return null
        val width = number(KEY_WIDTH)?.takeIf { it > 0 } ?: return null
        val height = number(KEY_HEIGHT)?.takeIf { it > 0 } ?: return null
        val topLeft = CompositorPointerScale.toAwt(x, y, scaleX, scaleY)
        val bottomRight = CompositorPointerScale.toAwt(x + width, y + height, scaleX, scaleY)
        return DesktopState.ScreenRect(
            left = topLeft.x.toDouble(),
            top = topLeft.y.toDouble(),
            right = bottomRight.x.toDouble(),
            bottom = bottomRight.y.toDouble(),
        )
    }
}

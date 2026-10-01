package org.mjdev.desktop.windows

import org.mjdev.desktop.log.Log
import java.awt.Frame
import java.awt.GraphicsDevice
import java.awt.Window

/**
 * Logs, once per window, every fact that decides whether a window can be see-through on this
 * machine: what the app asked for, what AWT really applied, and what the screen and session
 * support. Read the log lines prefixed with [TAG] to find which link of the chain is missing.
 */
object WindowTransparencyDiagnostics {
    /** Prefix of every diagnostic line, easy to grep in the session log. */
    private const val TAG = "WindowTransparency"

    /** Environment variables that influence rendering and the display session type. */
    private val ENV_KEYS = listOf("SKIKO_RENDER_API", "XDG_SESSION_TYPE", "WAYLAND_DISPLAY", "DISPLAY")

    /** Logs the transparency state of [window] named [name]; [requested] is what the app asked for. */
    fun log(
        name: String,
        window: Window,
        requested: Boolean,
    ) {
        if (!requested) return
        runCatching {
            val config = window.graphicsConfiguration
            val perPixel =
                config?.device?.isWindowTranslucencySupported(
                    GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT,
                )
            Log.i(
                "$TAG [$name] requested=$requested undecorated=${(window as? Frame)?.isUndecorated} " +
                    "backgroundAlpha=${window.background?.alpha} opaque=${window.isOpaque} " +
                    "gcTranslucencyCapable=${config?.isTranslucencyCapable} " +
                    "devicePerPixelTranslucency=$perPixel",
            )
            Log.i("$TAG [$name] env " + ENV_KEYS.joinToString(" ") { "$it=${System.getenv(it)}" })
        }.onFailure { e ->
            Log.e(e)
        }
    }
}

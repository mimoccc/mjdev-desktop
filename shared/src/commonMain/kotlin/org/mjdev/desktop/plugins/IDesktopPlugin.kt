package org.mjdev.desktop.plugins

import androidx.compose.runtime.Composable
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.plugins.remote.RemoteDocument
import org.mjdev.desktop.plugins.remote.RemoteVariables

/**
 * Contract implemented by every desktop widget plugin.
 *
 * A plugin describes its look as a [RemoteDocument] (data, not code) and feeds it live values
 * through [variables]; the desktop renders both with its own player. External plugins are jars
 * that register an implementation in `META-INF/services/org.mjdev.desktop.plugins.IDesktopPlugin`
 * and carry a `plugin.json` ([PluginMetadata]) at the jar root. One plugin per jar.
 */
interface IDesktopPlugin {
    /** The widget layout. Read once when the plugin is enabled. */
    val document: RemoteDocument

    /** Period in milliseconds between two calls of [variables]. */
    val refreshMs: Long
        get() = PluginDefaults.DEFAULT_REFRESH_MS

    /**
     * Optional interactive UI. When not null the desktop draws it instead of the [document] (the
     * document then only supplies size and anchor); used by built-in widgets that need buttons.
     */
    val content: (@Composable (IDesktopContext) -> Unit)?
        get() = null

    /** Produces the current values the [document] binds to. Called off the UI thread. */
    fun variables(context: IDesktopContext): RemoteVariables

    /** Called after the user enabled the plugin. */
    fun onEnabled() {}

    /** Called after the user disabled the plugin; release resources here. */
    fun onDisabled() {}
}

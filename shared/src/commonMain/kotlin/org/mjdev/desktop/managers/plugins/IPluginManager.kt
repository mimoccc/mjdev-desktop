package org.mjdev.desktop.managers.plugins

import org.mjdev.desktop.managers.base.IDelegate
import org.mjdev.desktop.plugins.IDesktopPlugin
import org.mjdev.desktop.plugins.PluginInfo

/**
 * Discovers desktop widget plugins in `~/.mjdev/plugins/`, remembers which ones the user enabled
 * and hands out live plugin instances. The [plugins] list is observable, so the control center
 * and the desktop update as soon as it changes.
 */
interface IPluginManager : IDelegate {
    /** Absolute path of the folder external plugin jars are read from. */
    val pluginsDir: String

    /** All known plugins (built-in and external) with their current enabled state. */
    val plugins: List<PluginInfo>

    /** Re-scans the plugins folder; keeps the enabled state of plugins that are still present. */
    fun refresh()

    /** Enables or disables the plugin [id] and persists the choice. */
    fun setEnabled(
        id: String,
        enabled: Boolean,
    )

    /** Returns the loaded instance of an enabled plugin, loading it on first use, or null. */
    fun instance(id: String): IDesktopPlugin?

    companion object {
        /** No-op fallback used when no platform manager is available. */
        val EMPTY =
            object : IPluginManager {
                override val pluginsDir: String = ""
                override val plugins: List<PluginInfo> = emptyList()

                override fun refresh() {}

                override fun setEnabled(
                    id: String,
                    enabled: Boolean,
                ) {}

                override fun instance(id: String): IDesktopPlugin? = null
            }
    }
}

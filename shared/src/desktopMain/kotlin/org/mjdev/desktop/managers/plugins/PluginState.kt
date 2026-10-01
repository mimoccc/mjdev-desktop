package org.mjdev.desktop.managers.plugins

import org.mjdev.desktop.plugins.MemoryWidgetPlugin

/**
 * Persisted plugin choices, stored as JSON in `~/.mjdev/desktop/plugins.json`. Defaults keep the
 * memory widget visible on a fresh install, matching the desktop before plugins existed.
 */
data class PluginState(
    /** Ids of the plugins the user switched on. */
    val enabled: MutableList<String> = mutableListOf(MemoryWidgetPlugin.ID),
)

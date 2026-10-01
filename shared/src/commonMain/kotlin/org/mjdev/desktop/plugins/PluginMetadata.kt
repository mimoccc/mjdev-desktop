package org.mjdev.desktop.plugins

/**
 * Descriptive data of a plugin, read from `plugin.json` inside the jar without executing any
 * plugin code. Every field has a default so Gson can always build it with a no-args constructor.
 */
data class PluginMetadata(
    /** Stable unique id, e.g. `com.example.clock`. Used to persist the enabled state. */
    val id: String = "",
    /** Label shown in the control center. */
    val name: String = "",
    /** Longer text shown when the plugin row is expanded. */
    val description: String = "",
    /** Plugin version string. */
    val version: String = "",
    /** Author or vendor name. */
    val author: String = "",
    /** Jar entry holding the plugin image; falls back to [PluginDefaults.DEFAULT_ICON_ENTRY]. */
    val icon: String = "",
)

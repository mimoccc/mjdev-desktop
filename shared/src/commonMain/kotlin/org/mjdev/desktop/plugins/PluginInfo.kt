package org.mjdev.desktop.plugins

import androidx.compose.ui.graphics.ImageBitmap

/** What the control center knows about one discovered plugin. */
data class PluginInfo(
    /** Descriptive metadata of the plugin. */
    val metadata: PluginMetadata,
    /** True when the user switched the plugin on, so it is shown on the desktop. */
    val enabled: Boolean = false,
    /** True for plugins shipped inside the desktop itself. */
    val builtIn: Boolean = false,
    /** Plugin image decoded from the jar, or null when it ships none. */
    val icon: ImageBitmap? = null,
    /** Where the plugin comes from: a jar path, or a label for built-in plugins. */
    val source: String = "",
    /** Last load error, or null when the plugin loaded fine (or was not loaded yet). */
    val error: String? = null,
) {
    /** Shortcut for `metadata.id`. */
    val id: String
        get() = metadata.id
}

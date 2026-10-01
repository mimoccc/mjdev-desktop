package org.mjdev.desktop.plugins

/** Constants describing where plugins live and how they are recognised on disk. */
object PluginDefaults {
    /** Directory name (under the `.mjdev` folder in the user's home) holding plugin jars. */
    const val PLUGINS_DIR_NAME = "plugins"

    /** File name of the persisted plugin state, stored beside the desktop config. */
    const val STATE_FILE_NAME = "plugins.json"

    /** Extension of a loadable plugin archive. */
    const val JAR_EXTENSION = "jar"

    /** Entry inside the jar that carries the [PluginMetadata] JSON. */
    const val METADATA_ENTRY = "plugin.json"

    /** Entry inside the jar used as the plugin image when metadata does not name one. */
    const val DEFAULT_ICON_ENTRY = "icon.png"

    /** Upper bound for a metadata or icon entry read from a jar, protects against huge entries. */
    const val MAX_ENTRY_BYTES = 1_048_576

    /** Default period in milliseconds between two variable refreshes of a widget. */
    const val DEFAULT_REFRESH_MS = 5_000L
}

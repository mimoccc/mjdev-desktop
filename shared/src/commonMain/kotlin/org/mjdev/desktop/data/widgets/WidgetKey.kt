package org.mjdev.desktop.data.widgets

/**
 * Builds the keys under which widget positions are saved, so every kind of widget has its own
 * namespace and keys never collide.
 *
 * @property prefix namespace of the key
 */
enum class WidgetKey(
    val prefix: String,
) {
    /** A plugin widget, keyed by plugin id. */
    Plugin("plugin:"),

    /** A desktop folder / file icon, keyed by its path. */
    Folder("folder:"),

    /** A built-in widget, keyed by its fixed name (see [MEMORY_CHART]). */
    BuiltIn("widget:"),
    ;

    /** Key of the widget identified by [id] within this namespace. */
    fun of(id: String): String = prefix + id

    companion object {
        /** Fixed name of the memory chart widget. */
        const val MEMORY_CHART = "memory-chart"
    }
}

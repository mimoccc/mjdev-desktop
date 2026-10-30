package org.psippr.desktop.components.desktop

/**
 * Items of the desktop's right-click menu.
 *
 * @property label text of the item
 */
enum class DesktopMenuItem(
    val label: String,
) {
    /** Puts the desktop icons back into the sorted grid. */
    SORT_ICONS("Sort icons"),
    ;

    companion object {
        /** The item labelled [label], or null. */
        fun of(label: String): DesktopMenuItem? = entries.firstOrNull { item -> item.label == label }
    }
}

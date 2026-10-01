package org.mjdev.desktop.plugins.remote

import androidx.compose.ui.Alignment

/** Placement of a document or text inside its bounds, parsed from a plain string in the JSON. */
enum class RemoteAlign(
    /** Matching Compose alignment for placing a whole widget. */
    val alignment: Alignment,
) {
    TOP_START(Alignment.TopStart),
    TOP_CENTER(Alignment.TopCenter),
    TOP_END(Alignment.TopEnd),
    CENTER_START(Alignment.CenterStart),
    CENTER(Alignment.Center),
    CENTER_END(Alignment.CenterEnd),
    BOTTOM_START(Alignment.BottomStart),
    BOTTOM_CENTER(Alignment.BottomCenter),
    BOTTOM_END(Alignment.BottomEnd),
    ;

    companion object {
        /** Parses [value] ignoring case and separators, falling back to [default]. */
        fun parse(
            value: String,
            default: RemoteAlign = CENTER,
        ): RemoteAlign = entries.firstOrNull {
            it.name.replace("_", "").equals(value.replace("_", ""), true)
        } ?: default
    }
}

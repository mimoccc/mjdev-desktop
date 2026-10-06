package org.mjdev.desktop.data.widgets

import androidx.compose.ui.geometry.Offset

/**
 * Saved drag offset of one desktop widget, in pixels, relative to its default place.
 *
 * @property x horizontal offset
 * @property y vertical offset
 */
data class WidgetPosition(
    var x: Float = 0f,
    var y: Float = 0f,
) {
    /** The position as a Compose [Offset]. */
    fun toOffset(): Offset = Offset(x, y)

    companion object {
        /** Position of the given [offset]. */
        fun of(offset: Offset): WidgetPosition = WidgetPosition(offset.x, offset.y)
    }
}

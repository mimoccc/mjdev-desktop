package org.psippr.desktop.managers.theme.linux.gtk

import androidx.compose.ui.graphics.Color

/**
 * Semantic status colors - kept at the stock GNOME / Ubuntu values so errors and warnings
 * look the same as on a stock install whatever the wallpaper is.
 *
 * @property bg background (button / banner fill)
 * @property fg text on [bg]
 */
enum class GtkStatusColor(
    val bg: Color,
    val fg: Color,
) {
    /** Destructive actions (delete buttons). */
    DESTRUCTIVE(Color(0xFFE01B24), Color.White),

    /** Success state. */
    SUCCESS(Color(0xFF2EC27E), Color.White),

    /** Warning state. */
    WARNING(Color(0xFFE5A50A), Color(0xCC000000)),

    /** Error state. */
    ERROR(Color(0xFFE01B24), Color.White),
}

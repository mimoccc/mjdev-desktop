package org.psippr.desktop.managers.palette

import androidx.compose.ui.graphics.Color

/**
 * Accent of the desktop (icons, widget rings, selection): the wallpaper's own, or a fixed color.
 * A fixed color is still made readable on the background the same way as a wallpaper accent.
 *
 * @property label name shown in the theme settings
 * @property color the fixed color, null for the wallpaper's accent
 */
enum class ThemeAccent(
    val label: String,
    val color: Color?,
) {
    /** The most colorful part of the wallpaper (the original behaviour). */
    WALLPAPER("From wallpaper", null),
    BLUE("Blue", Color(0xFF3584E4)),
    TEAL("Teal", Color(0xFF2190A4)),
    GREEN("Green", Color(0xFF3A944A)),
    YELLOW("Yellow", Color(0xFFC88800)),
    ORANGE("Orange", Color(0xFFED5B00)),
    RED("Red", Color(0xFFE62D42)),
    PINK("Pink", Color(0xFFD56199)),
    PURPLE("Purple", Color(0xFF9141AC)),
    ;

    companion object {
        /** Accent stored under [name], [WALLPAPER] for an unknown or missing value. */
        fun of(name: String?): ThemeAccent = entries.firstOrNull { accent -> accent.name == name } ?: WALLPAPER
    }
}

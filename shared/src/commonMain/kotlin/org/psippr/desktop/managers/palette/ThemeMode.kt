package org.psippr.desktop.managers.palette

/**
 * How light the desktop is. The wallpaper still gives the hue; dark / light only fix the
 * lightness of the background, text and border follow it.
 *
 * @property label name shown in the theme settings
 */
enum class ThemeMode(
    val label: String,
) {
    /** Light or dark as the wallpaper's corners are (the original behaviour). */
    WALLPAPER("From wallpaper"),

    /** Always a dark background. */
    DARK("Dark"),

    /** Always a light background. */
    LIGHT("Light"),
    ;

    companion object {
        /** Mode stored under [name], [WALLPAPER] for an unknown or missing value. */
        fun of(name: String?): ThemeMode = entries.firstOrNull { mode -> mode.name == name } ?: WALLPAPER
    }
}

package org.psippr.desktop.managers.theme.linux.icons

/**
 * Fixed colors of generated icons: a glyph on a circle, the same for every wallpaper.
 *
 * @property argb color as 0xAARRGGBB
 */
enum class IconThemeColor(
    val argb: Int,
) {
    /** The circle behind the glyph (blue grey). */
    CIRCLE(0xFF546E7A.toInt()),

    /** The glyph. */
    GLYPH(0xFFFFFFFF.toInt()),
}

package org.psippr.desktop.managers.theme.linux.icons

import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Typeface

/**
 * Draws icon font glyphs as PNG icons: the glyph centered on a circle, in [IconThemeColor]s.
 *
 * @param ttf icon font file contents
 */
class GlyphIconRenderer(
    ttf: ByteArray,
) {
    private val typeface: Typeface? =
        runCatching { FontMgr.default.makeFromData(Data.makeFromBytes(ttf)) }.getOrNull()

    /** PNG of the glyph [codepoint] at [px] pixels, or null when the font cannot be read. */
    fun render(
        codepoint: Int,
        px: Int,
    ): ByteArray? {
        val face = typeface ?: return null
        val surface = Surface.makeRasterN32Premul(px, px)
        val canvas = surface.canvas
        val center = px / HALF
        canvas.drawCircle(center, center, center, paint(IconThemeColor.CIRCLE))
        val font = Font(face, px * GLYPH_RATIO)
        val text = String(Character.toChars(codepoint))
        val bounds = font.measureText(text)
        val x = center - bounds.width / HALF - bounds.left
        val y = center - bounds.height / HALF - bounds.top
        canvas.drawString(text, x, y, font, paint(IconThemeColor.GLYPH))
        return surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)?.bytes
    }

    private fun paint(color: IconThemeColor) = Paint().apply {
        this.color = color.argb
        isAntiAlias = true
    }

    private companion object {
        /** Half of an edge: the circle's center and radius. */
        const val HALF = 2f

        /** Glyph size relative to the icon edge, leaving the circle's rim free. */
        const val GLYPH_RATIO = 0.6f
    }
}

package org.psippr.desktop.managers.theme.linux.icons

import org.psippr.desktop.helpers.font.CodePointsFile
import org.psippr.desktop.helpers.font.MaterialIconFont
import org.psippr.desktop.helpers.streams.ResourceStream
import org.psippr.desktop.interfaces.IFont

/**
 * An icon font a theme is generated from; the font file and code points are read on first use.
 *
 * @property name font name, part of the theme name (`MaterialSymbolsSharp`)
 */
class IconFontSource(
    val name: String,
    readTtf: () -> ByteArray,
    readCodepoints: () -> Map<String, Int>,
) {
    /** The font file contents. */
    val ttf: ByteArray by lazy(readTtf)

    /** Glyph code point of each glyph name. */
    val codepoints: Map<String, Int> by lazy(readCodepoints)

    companion object {
        /** Folder separator of resource names. */
        private const val SEPARATOR = '/'

        /** Separator of a file's extension. */
        private const val EXTENSION = '.'

        /** Source of [font], or null when it is no font file with code points. */
        fun of(font: IFont): IconFontSource? {
            val material = font as? MaterialIconFont ?: return null
            return IconFontSource(
                name = material.ttfFileName.substringAfterLast(SEPARATOR).substringBeforeLast(EXTENSION),
                readTtf = { ResourceStream(material.ttfFileName).bytes },
                readCodepoints = { CodePointsFile(material.codepointsFileName).icons },
            )
        }
    }
}

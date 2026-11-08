package org.psippr.desktop.managers.theme.linux.icons

import org.psippr.desktop.helpers.font.MaterialSymbolsOutlined
import org.psippr.desktop.helpers.font.MaterialSymbolsRounded
import org.psippr.desktop.helpers.font.MaterialSymbolsSharp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Every listed icon has its glyph in every shipped icon font, and no name is listed twice. */
class FreedesktopIconTest {
    private val fonts = listOf(MaterialSymbolsOutlined, MaterialSymbolsRounded, MaterialSymbolsSharp)

    @Test
    fun everyGlyphIsInEveryFont() {
        fonts.forEach { font ->
            val source = IconFontSource.of(font) ?: error("no source for $font")
            val missing = FreedesktopIcon.entries.filter { icon -> icon.glyph !in source.codepoints }
            assertTrue(missing.isEmpty(), "${source.name} lacks ${missing.map { icon -> icon.glyph }}")
        }
    }

    @Test
    fun namesAreUniquePerContext() {
        val keys = FreedesktopIcon.entries.map { icon -> icon.context to icon.iconName }
        assertEquals(keys.size, keys.toSet().size)
    }
}

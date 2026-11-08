package org.psippr.desktop.managers.theme.linux.icons

import org.psippr.desktop.log.Log
import java.io.File

/**
 * Makes a freedesktop icon theme from an icon font, once per font: every [FreedesktopIcon] as a
 * glyph on a circle in every [IconSize]. Icons it lacks come from the theme it inherits.
 *
 * @property iconsDir folder holding user icon themes (`~/.local/share/icons`)
 */
class IconThemeGenerator(
    private val iconsDir: File,
) {
    /** Name of the theme made from [fontName] (`Psippr-MaterialSymbolsSharp`). */
    fun themeName(fontName: String): String = "$THEME_PREFIX$fontName"

    /** True when [name] is a theme made by this generator. */
    fun isGenerated(name: String?): Boolean = name?.startsWith(THEME_PREFIX) == true

    /**
     * Makes the theme of [font] unless it is already there, inheriting [inherits]; returns its
     * name, or null when it could not be made. Blocks for the drawing.
     */
    fun generate(
        font: IconFontSource,
        inherits: String,
    ): String? {
        val name = themeName(font.name)
        val dir = File(iconsDir, name)
        val index = File(dir, IconThemeIndex.FILE_NAME)
        if (index.isFile && IconThemeIndex.isCurrent(index.readText())) return name
        return runCatching {
            val renderer = GlyphIconRenderer(font.ttf)
            FreedesktopIcon.entries.forEach { icon ->
                val codepoint = font.codepoints[icon.glyph] ?: return@forEach
                IconSize.entries.forEach { size ->
                    renderer.render(codepoint, size.px)?.let { png ->
                        File(dir, "${IconThemeIndex.dirOf(size, icon.context)}/${icon.iconName}$PNG").apply {
                            parentFile.mkdirs()
                            writeBytes(png)
                        }
                    }
                }
            }
            // written last: a theme cut short is made again next time
            index.writeText(IconThemeIndex.build(name, font.name, inherits))
            Log.d("Icon theme generated: $name in ${dir.absolutePath}")
            name
        }.onFailure { e -> Log.e(e) }.getOrNull()
    }

    private companion object {
        /** Start of every generated theme name. */
        const val THEME_PREFIX = "Psippr-"

        /** Ending of the icon files. */
        const val PNG = ".png"
    }
}

package org.psippr.desktop.managers.theme.linux.icons

import org.psippr.desktop.helpers.font.MaterialSymbolsSharp
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A theme is drawn from the icon font once and then reused. */
class IconThemeGeneratorTest {
    private val iconsDir: File = Files.createTempDirectory("icons").toFile()
    private val generator = IconThemeGenerator(iconsDir)
    private val font = IconFontSource.of(MaterialSymbolsSharp) ?: error("no font source")

    @AfterTest
    fun cleanUp() {
        iconsDir.deleteRecursively()
    }

    @Test
    fun folderIconIsDrawnAsPng() {
        val name = generator.generate(font, inherits = "Adwaita")
        assertEquals("Psippr-MaterialSymbolsSharp", name)
        val png = File(iconsDir, "$name/48x48/places/folder.png")
        assertTrue(png.isFile)
        // PNG signature
        assertEquals(0x89.toByte(), png.readBytes().first())
    }

    @Test
    fun existingThemeIsNotDrawnAgain() {
        val name = generator.generate(font, inherits = "Adwaita")
        val png = File(iconsDir, "$name/48x48/places/folder.png")
        png.delete()
        generator.generate(font, inherits = "Adwaita")
        assertTrue(!png.exists())
    }
}

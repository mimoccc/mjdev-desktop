package org.psippr.desktop.managers.theme.linux

import okio.Path.Companion.toOkioPath
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression test: unchanged theme content must not trigger a live reload. */
class ThemeFileWriterTest {
    @Test
    fun writesOnlyWhenContentChanges() {
        val dir = createTempDirectory().toFile()
        val file = dir.resolve("theme/gtk-3.0/gtk.css").toOkioPath()
        assertTrue(ThemeFileWriter.writeIfChanged(file, "a"))
        assertFalse(ThemeFileWriter.writeIfChanged(file, "a"))
        assertTrue(ThemeFileWriter.writeIfChanged(file, "b"))
        dir.deleteRecursively()
    }
}

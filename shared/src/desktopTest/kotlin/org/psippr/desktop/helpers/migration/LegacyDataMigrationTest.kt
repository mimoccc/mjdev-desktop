package org.psippr.desktop.helpers.migration

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression test: upgrading from mjdev-desktop keeps the user's data and never overwrites. */
class LegacyDataMigrationTest {
    @Test
    fun oldDataIsMovedToNewPlace() {
        val home = Files.createTempDirectory(TEMP_PREFIX).toFile()
        File(home, ".mjdev/desktop").mkdirs()
        File(home, ".mjdev/desktop/config.json").writeText(CONTENT)
        val moved = LegacyDataMigration.migrate(home)
        assertEquals(listOf(LegacyDataDir.Config), moved)
        assertEquals(CONTENT, File(home, ".psippr/desktop/config.json").readText())
        assertFalse(File(home, ".mjdev").exists())
        home.deleteRecursively()
    }

    @Test
    fun existingNewDataIsNeverOverwritten() {
        val home = Files.createTempDirectory(TEMP_PREFIX).toFile()
        File(home, ".mjdev").mkdirs()
        File(home, ".psippr").mkdirs()
        File(home, ".psippr/keep").writeText(CONTENT)
        assertTrue(LegacyDataMigration.migrate(home).isEmpty())
        assertTrue(File(home, ".mjdev").exists())
        assertEquals(CONTENT, File(home, ".psippr/keep").readText())
        home.deleteRecursively()
    }

    private companion object {
        const val TEMP_PREFIX = "psippr-migration"
        const val CONTENT = "{\"kept\":true}"
    }
}

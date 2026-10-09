package org.psippr.desktop.helpers.migration

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression test: upgrading keeps the user's data, never overwrites and never mixes projects. */
class LegacyDataMigrationTest {
    @Test
    fun oldDataIsMovedToNewPlace() {
        val home = Files.createTempDirectory(TEMP_PREFIX).toFile()
        File(home, ".mjdev/desktop").mkdirs()
        File(home, ".mjdev/desktop/config.json").writeText(CONTENT)
        val moved = LegacyDataMigration.migrate(home)
        assertEquals(listOf(LegacyDataDir.Config), moved)
        assertEquals(CONTENT, File(home, ".psippr-desktop/desktop/config.json").readText())
        assertFalse(File(home, ".mjdev").exists())
        home.deleteRecursively()
    }

    @Test
    fun existingNewDataIsNeverOverwritten() {
        val home = Files.createTempDirectory(TEMP_PREFIX).toFile()
        File(home, ".mjdev").mkdirs()
        File(home, ".psippr-desktop").mkdirs()
        File(home, ".psippr-desktop/keep").writeText(CONTENT)
        assertTrue(LegacyDataMigration.migrate(home).isEmpty())
        assertTrue(File(home, ".mjdev").exists())
        assertEquals(CONTENT, File(home, ".psippr-desktop/keep").readText())
        home.deleteRecursively()
    }

    /** Only this app's sub folders leave `~/.psippr`, anything of another project stays there. */
    @Test
    fun foreignPsipprDataStaysInPlace() {
        val home = Files.createTempDirectory(TEMP_PREFIX).toFile()
        File(home, ".psippr/desktop").mkdirs()
        File(home, ".psippr/desktop/config.json").writeText(CONTENT)
        File(home, ".psippr/other").mkdirs()
        File(home, ".psippr/other/data").writeText(CONTENT)
        val moved = LegacyDataMigration.migrate(home)
        assertEquals(listOf(LegacyDataDir.PsipprConfig), moved)
        assertEquals(CONTENT, File(home, ".psippr-desktop/desktop/config.json").readText())
        assertEquals(CONTENT, File(home, ".psippr/other/data").readText())
        home.deleteRecursively()
    }

    private companion object {
        const val TEMP_PREFIX = "psippr-migration"
        const val CONTENT = "{\"kept\":true}"
    }
}

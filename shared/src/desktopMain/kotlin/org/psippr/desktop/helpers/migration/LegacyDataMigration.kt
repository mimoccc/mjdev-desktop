package org.psippr.desktop.helpers.migration

import java.io.File
import java.nio.file.Files

/**
 * Carries user data over from the old mjdev-desktop name: each [LegacyDataDir] is moved (renamed,
 * never deleted) to its new place when the old directory exists and the new one does not yet.
 * Runs before anything reads config or writes the cache, so nothing is lost on upgrade.
 */
object LegacyDataMigration {
    /** Moves every legacy directory under [home]; returns the ones that were moved. */
    fun migrate(home: File = File(System.getProperty(HOME_PROPERTY))): List<LegacyDataDir> =
        LegacyDataDir.entries.filter { dir -> move(File(home, dir.oldPath), File(home, dir.newPath)) }

    // true when old was moved to new; an existing new directory is never overwritten
    private fun move(
        old: File,
        new: File,
    ): Boolean = runCatching {
        if (!old.isDirectory || new.exists()) return false
        new.parentFile?.mkdirs()
        Files.move(old.toPath(), new.toPath())
        true
    }.getOrElse { e ->
        System.err.println("$LOG_PREFIX ${old.path} -> ${new.path} failed: ${e.message}")
        false
    }

    /** JVM property holding the user's home directory. */
    private const val HOME_PROPERTY = "user.home"

    /** Prefix of the stderr line when a move fails (runs before the logger is set up). */
    private const val LOG_PREFIX = "psippr data migration:"
}

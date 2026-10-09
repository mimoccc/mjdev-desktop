package org.psippr.desktop.helpers.migration

/**
 * User data directories of the old mjdev-desktop name and their psippr-desktop replacements,
 * relative to the user's home. Moved once by [LegacyDataMigration] when only the old one exists.
 *
 * @property oldPath directory used before the rename
 * @property newPath directory used now
 */
enum class LegacyDataDir(
    val oldPath: String,
    val newPath: String,
) {
    /** Desktop config, plugins and widget positions. */
    Config(".mjdev", ".psippr"),

    /** Session log and font guard config. */
    Cache(".cache/mjdev-desktop", ".cache/psippr-desktop"),

    /** Compositor window geometry. */
    State(".local/state/mjdev", ".local/state/psippr"),
}

package org.psippr.desktop.helpers.migration

/**
 * User data of older names of this app and their psippr-desktop replacements, relative to the
 * user's home. Moved once by [LegacyDataMigration] when the old one exists and the new one not.
 * Only this app's own data is listed: a folder another project may use (`~/.psippr`) is never
 * moved as a whole, only the sub folders this app wrote into it.
 *
 * @property oldPath file or directory used before
 * @property newPath file or directory used now
 */
enum class LegacyDataDir(
    val oldPath: String,
    val newPath: String,
) {
    // old: targets named after the separate psippr project, so this app moved its data into a
    // folder that other project may use
//    Config(".mjdev", ".psippr"),
//    Cache(".cache/mjdev-desktop", ".cache/psippr-desktop"),
//    State(".local/state/mjdev", ".local/state/psippr"),

    /** Desktop config, plugins and widget positions of the mjdev-desktop name. */
    Config(".mjdev", ".psippr-desktop"),

    /** Desktop config written to `~/.psippr` by the first psippr build. */
    PsipprConfig(".psippr/desktop", ".psippr-desktop/desktop"),

    /** Plugin jars written to `~/.psippr` by the first psippr build. */
    PsipprPlugins(".psippr/plugins", ".psippr-desktop/plugins"),

    /** Session log and font guard config. */
    Cache(".cache/mjdev-desktop", ".cache/psippr-desktop"),
}

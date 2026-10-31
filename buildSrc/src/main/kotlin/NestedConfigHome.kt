import java.io.File
import java.nio.file.Files

/**
 * Private config folder (`XDG_CONFIG_HOME`) of a nested test run, so its theme recolors the nested
 * apps without touching the host desktop. What the theme writes (GNOME settings in dconf, the GTK
 * stylesheets) is copied once from the user's `~/.config`; every other entry links to the real one,
 * so apps keep their settings and nothing large or secret is duplicated.
 */
object NestedConfigHome {
    /** The user's config folder under the home folder. */
    private const val USER_CONFIG = ".config"

    /** Where the private folder lives under the home folder. */
    private const val NESTED_CONFIG = ".cache/psippr-desktop/nested-config"

    /** Entries the theme writes: copied, so changes stay in the nested run. */
    private val COPIED = setOf("dconf", "gtk-2.0", "gtk-3.0", "gtk-4.0")

    /** Prepares the private folder for the user with [home] and returns it. */
    fun prepare(home: File): File {
        val source = File(home, USER_CONFIG)
        val target = File(home, NESTED_CONFIG).apply { mkdirs() }
        source.listFiles().orEmpty().forEach { entry ->
            val copy = File(target, entry.name)
            // a link left as a dangling or existing entry is kept: filled once
            if (copy.exists() || Files.isSymbolicLink(copy.toPath())) return@forEach
            runCatching {
                if (entry.name in COPIED) {
                    entry.copyRecursively(copy)
                } else {
                    Files.createSymbolicLink(copy.toPath(), entry.toPath())
                }
            }
        }
        // the theme writes these even when the user has none yet
        COPIED.forEach { name -> File(target, name).mkdirs() }
        return target
    }
}

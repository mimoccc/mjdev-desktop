package org.psippr.desktop.helpers.files

/**
 * Freedesktop base directories, each read from its environment variable or, when that is unset,
 * from the spec's default.
 *
 * @property variable environment variable holding the directory (or a `:` list of them)
 * @property fallback default when the variable is unset; a relative one is under the home folder
 */
enum class XdgDir(
    val variable: String,
    val fallback: String,
) {
    /** The user's config folder. */
    CONFIG_HOME("XDG_CONFIG_HOME", ".config"),

    /** System config folders. */
    CONFIG_DIRS("XDG_CONFIG_DIRS", "/etc/xdg"),

    /** The user's data folder. */
    DATA_HOME("XDG_DATA_HOME", ".local/share"),

    /** System data folders. */
    DATA_DIRS("XDG_DATA_DIRS", "/usr/local/share:/usr/share"),
    ;

    /** The folders, with [env] giving environment variables and [home] the home folder. */
    fun paths(
        env: (String) -> String?,
        home: String,
    ): List<String> = (env(variable)?.takeIf { value -> value.isNotBlank() } ?: fallback)
        .split(LIST_SEPARATOR)
        .filter { path -> path.isNotBlank() }
        .map { path -> if (path.startsWith(ROOT)) path else "$home$ROOT$path" }

    private companion object {
        /** Separates folders in one variable. */
        const val LIST_SEPARATOR = ':'

        /** Start of an absolute path. */
        const val ROOT = "/"
    }
}

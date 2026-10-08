package org.psippr.desktop.managers.theme.linux.shell

import java.io.File

/**
 * GNOME Shell "User Themes" extension (`gnome-shell-extension-user-theme` / extensions.gnome.org),
 * the standard way to load a shell stylesheet from `~/.themes/<name>/gnome-shell`.
 * Its gsettings schema ships inside the extension, so its compiled schema dir is needed.
 */
object UserThemeExtension {
    /** Extension uuid. */
    const val UUID = "user-theme@gnome-shell-extensions.gcampax.github.com"

    /** Sub directory with the compiled schema. */
    private const val SCHEMAS_DIR = "schemas"

    /** Compiled schema file. */
    private const val COMPILED_SCHEMAS = "gschemas.compiled"

    /** Per-user extension dir, relative to the home dir. */
    private const val USER_EXTENSIONS_DIR = ".local/share/gnome-shell/extensions"

    /** System extension dir. */
    private const val SYSTEM_EXTENSIONS_DIR = "/usr/share/gnome-shell/extensions"

    /** System wide compiled schemas (used when the distro package installed the schema there). */
    private const val SYSTEM_SCHEMAS_DIR = "/usr/share/glib-2.0/schemas"

    /** Schema file name installed system wide by the distro package. */
    private const val SYSTEM_SCHEMA_FILE = "org.gnome.shell.extensions.user-theme.gschema.xml"

    /**
     * Schema dir to pass to gsettings `--schemadir`: the extension's own dir, or null (default path)
     * when the schema is installed system wide; [NOT_INSTALLED] when the extension is missing.
     */
    fun schemaDir(homeDir: File): Result<String?> {
        val bundled =
            listOf(File(homeDir, USER_EXTENSIONS_DIR), File(SYSTEM_EXTENSIONS_DIR))
                .map { dir -> File(dir, "$UUID/$SCHEMAS_DIR") }
                .firstOrNull { dir -> File(dir, COMPILED_SCHEMAS).isFile }
        return when {
            bundled != null -> Result.success(bundled.absolutePath)
            File(SYSTEM_SCHEMAS_DIR, SYSTEM_SCHEMA_FILE).isFile -> Result.success(null)
            else -> Result.failure(NOT_INSTALLED)
        }
    }

    /** Reason used when the extension is not installed. */
    val NOT_INSTALLED = IllegalStateException("GNOME Shell User Themes extension ($UUID) not installed")
}

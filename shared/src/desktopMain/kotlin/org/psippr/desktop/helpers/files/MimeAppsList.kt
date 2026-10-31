package org.psippr.desktop.helpers.files

/**
 * Reads the `[Default Applications]` group of a freedesktop `mimeapps.list` file
 * (`inode/directory=org.gnome.Nautilus.desktop;`).
 */
object MimeAppsList {
    /** Group holding the default app of each type. */
    private const val DEFAULTS_GROUP = "[Default Applications]"

    /** First character of a group header. */
    private const val GROUP_START = '['

    /** Starts a comment line. */
    private const val COMMENT = '#'

    /** Separates a type from its apps. */
    private const val KEY_SEPARATOR = '='

    /** Separates the apps of one type. */
    private const val APP_SEPARATOR = ';'

    /** Desktop entry ids set as default for [mimeType] in the list [text], most wanted first. */
    fun defaults(
        text: String,
        mimeType: String,
    ): List<String> {
        var inDefaults = false
        text.lineSequence().map { line -> line.trim() }.forEach { line ->
            when {
                line.isEmpty() || line.first() == COMMENT -> Unit
                line.first() == GROUP_START -> inDefaults = line == DEFAULTS_GROUP
                inDefaults && line.substringBefore(KEY_SEPARATOR).trim() == mimeType ->
                    return line
                        .substringAfter(KEY_SEPARATOR)
                        .split(APP_SEPARATOR)
                        .map { id -> id.trim() }
                        .filter { id -> id.isNotEmpty() }
            }
        }
        return emptyList()
    }
}

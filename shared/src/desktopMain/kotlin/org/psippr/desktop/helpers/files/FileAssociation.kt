package org.psippr.desktop.helpers.files

import org.psippr.desktop.helpers.system.shell.Shell
import java.io.File

/**
 * The app the system associates with a file, as `xdg-mime` reports it (the user's and the
 * system's mimeapps.list, the same answer file managers and `xdg-open` use).
 */
object FileAssociation {
    /** Tool answering type and default app queries. */
    private const val CMD_XDG_MIME = "xdg-mime"

    /** `xdg-mime` arguments. */
    private const val QUERY = "query"
    private const val FILE_TYPE = "filetype"
    private const val DEFAULT = "default"

    /** Ending of a desktop entry id. */
    private const val DESKTOP_SUFFIX = ".desktop"

    /**
     * Desktop file of the installed default app for [path], or null when none is set. Blocks.
     * Unlike [defaultApp] it skips defaults that are not installed and also asks the distro's
     * lists, because `xdg-mime` in a psippr session falls back to any app claiming the type.
     */
    fun defaultAppFile(
        path: String,
        lookup: MimeAppsLookup = MimeAppsLookup(),
    ): File? {
        val mimeType = Shell.executeAndRead(CMD_XDG_MIME, QUERY, FILE_TYPE, path).trim()
        if (mimeType.isEmpty()) return null
        return lookup.defaultApp(mimeType)
    }

    /** Desktop entry id of the default app for [path], or null when none is set. Blocks. */
    fun defaultApp(path: String): String? {
        val mimeType = Shell.executeAndRead(CMD_XDG_MIME, QUERY, FILE_TYPE, path).trim()
        if (mimeType.isEmpty()) return null
        return appIdOf(Shell.executeAndRead(CMD_XDG_MIME, QUERY, DEFAULT, mimeType))
    }

    /** The desktop entry id in an `xdg-mime query default` [output], null when it names none. */
    fun appIdOf(output: String): String? = output
        .lineSequence()
        .map { line -> line.trim() }
        .firstOrNull { line -> line.endsWith(DESKTOP_SUFFIX) }
}

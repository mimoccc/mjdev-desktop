package org.psippr.desktop.helpers.files

import org.psippr.desktop.log.Log

/**
 * Opens a file or folder with the app associated to its type, only when there is one. The app
 * is started detached: the generic `xdg-open` (outside GNOME/KDE) waits until the app exits.
 */
object FileOpener {
    /** Tool starting the associated app with the file. */
    private const val CMD_XDG_OPEN = "xdg-open"

    /** Opens [path]; blocks only for the association lookup. */
    fun open(path: String): FileOpenResult {
        if (FileAssociation.defaultApp(path) == null) return FileOpenResult.NO_APP
        return runCatching {
            ProcessBuilder(CMD_XDG_OPEN, path)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start()
        }.fold(
            onSuccess = { FileOpenResult.OPENED },
            onFailure = { e ->
                Log.e(e)
                FileOpenResult.FAILED
            },
        )
    }
}

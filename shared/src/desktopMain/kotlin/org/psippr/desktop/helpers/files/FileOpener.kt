package org.psippr.desktop.helpers.files

import okio.Path.Companion.toPath
import org.psippr.desktop.data.DesktopFile
import org.psippr.desktop.helpers.system.environment.InheritedGdkBackend
import org.psippr.desktop.log.Log

/**
 * Opens a file or folder with the app associated to its type, only when there is one. The app
 * is started detached: the generic `xdg-open` (outside GNOME/KDE) waits until the app exits.
 */
object FileOpener {
    /** Tool starting the associated app with the file. */
    private const val CMD_XDG_OPEN = "xdg-open"

    /** Tool starting the app of a desktop file with the file (`gio launch <entry> <file>`). */
    private const val CMD_GIO = "gio"
    private const val GIO_LAUNCH = "launch"

    /** Opens [path]; blocks only for the association lookup. */
    fun open(path: String): FileOpenResult {
        // old: the app check and xdg-open in a psippr session took any app claiming the type
//        if (FileAssociation.defaultApp(path) == null) return FileOpenResult.NO_APP
//        return runCatching {
//            ProcessBuilder(CMD_XDG_OPEN, path)
        val app = FileAssociation.defaultAppFile(path) ?: return FileOpenResult.NO_APP
        // old: gio launch asks a D-Bus activatable app (Nautilus) over the bus and exits at once;
        // an app not running yet never got the request (nested psippr session) and quit idle
//        return runCatching {
//            ProcessBuilder(CMD_GIO, GIO_LAUNCH, app.path, path)
        val exec = DesktopFile(app.path.toPath()).desktopSection?.Exec.orEmpty()
        val command = ExecLine.command(exec, path).ifEmpty { return FileOpenResult.NO_APP }
        return runCatching {
            ProcessBuilder(command)
                .apply { InheritedGdkBackend.restore(environment()) }
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

package org.psippr.desktop.managers.notifications

import okio.Path
import org.psippr.desktop.data.DesktopConfigStore
import org.psippr.desktop.extensions.PathExt.exists
import org.psippr.desktop.extensions.PathExt.mkdirs
import org.psippr.desktop.extensions.PathExt.parentFile
import org.psippr.desktop.extensions.PathExt.text
import org.psippr.desktop.extensions.PathExt.writeText
import org.psippr.desktop.helpers.generic.JsonHelper.fromJson
import org.psippr.desktop.helpers.generic.JsonHelper.toJson
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.log.Log

/** Keeps the [NotificationHistory] beside the desktop config, so it survives a restart. */
class NotificationStore(
    user: IUser,
) {
    private val file: Path =
        user.homeDir
            .resolve(DesktopConfigStore.CONFIG_DIR_NAME)
            .resolve(DesktopConfigStore.CONFIG_SUBDIR_NAME)
            .resolve(FILE_NAME)

    /** The stored history, empty when none or unreadable. */
    fun load(): NotificationHistory = runCatching {
        if (file.exists) fromJson<NotificationHistory>(file.text) else null
    }.onFailure { e -> Log.e(e) }.getOrNull() ?: NotificationHistory()

    /** Stores [history]. */
    fun save(history: NotificationHistory) {
        runCatching {
            file.parentFile.mkdirs()
            file.writeText(history.toJson())
        }.onFailure { e -> Log.e(e) }
    }

    private companion object {
        /** File of the history beside the desktop config. */
        const val FILE_NAME = "notifications.json"
    }
}

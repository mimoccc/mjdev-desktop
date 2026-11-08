package org.psippr.desktop.managers.devices

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

/** Keeps [KnownDevices] beside the desktop config. */
class KnownDevicesStore(
    user: IUser,
) {
    private val file: Path =
        user.homeDir
            .resolve(DesktopConfigStore.CONFIG_DIR_NAME)
            .resolve(DesktopConfigStore.CONFIG_SUBDIR_NAME)
            .resolve(FILE_NAME)

    /** The stored devices, none when nothing is stored or it is unreadable. */
    fun load(): KnownDevices = runCatching {
        if (file.exists) fromJson<KnownDevices>(file.text) else null
    }.onFailure { e -> Log.e(e) }.getOrNull() ?: KnownDevices()

    /** Stores [devices]. */
    fun save(devices: KnownDevices) {
        runCatching {
            file.parentFile.mkdirs()
            file.writeText(devices.toJson())
        }.onFailure { e -> Log.e(e) }
    }

    private companion object {
        /** File of the remembered devices beside the desktop config. */
        const val FILE_NAME = "known-devices.json"
    }
}

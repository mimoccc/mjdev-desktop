package org.psippr.desktop.data

import okio.Path
import org.psippr.desktop.extensions.PathExt.exists
import org.psippr.desktop.extensions.PathExt.mkdirs
import org.psippr.desktop.extensions.PathExt.parentFile
import org.psippr.desktop.extensions.PathExt.text
import org.psippr.desktop.extensions.PathExt.writeText
import org.psippr.desktop.helpers.generic.JsonHelper.fromJson
import org.psippr.desktop.helpers.generic.JsonHelper.toJson
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.log.Log

/**
 * Reads and writes the whole [DesktopConfigData] as JSON at `~/.psippr/desktop/config.json`.
 * A missing or corrupt file falls back to defaults, so the desktop always has a valid config.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class DesktopConfigStore(
    private val user: IUser,
) {
    val configFile: Path =
        user.homeDir
            .resolve(CONFIG_DIR_NAME)
            .resolve(CONFIG_SUBDIR_NAME)
            .resolve(CONFIG_FILE_NAME)

    fun load(): DesktopConfigData = runCatching {
        if (configFile.exists) {
            fromJson<DesktopConfigData>(configFile.text)
        } else {
            null
        }
    }.onFailure { e ->
        Log.e(e)
    }.getOrNull() ?: DesktopConfigData()

    fun save(data: DesktopConfigData) = runCatching {
        configFile.parentFile.mkdirs()
        configFile.writeText(data.toJson())
        Log.d("Desktop config saved to $configFile")
    }.onFailure { e ->
        Log.e(e)
    }.let {}

    companion object {
        const val CONFIG_DIR_NAME = ".psippr"
        const val CONFIG_SUBDIR_NAME = "desktop"
        const val CONFIG_FILE_NAME = "config.json"
    }
}

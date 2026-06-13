/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.apps

import kotlinx.coroutines.CoroutineScope
import okio.Path
import okio.Path.Companion.toPath
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.data.App
import org.mjdev.desktop.extensions.Custom.desktopFiles
import org.mjdev.desktop.extensions.Custom.textAsLocale
import org.mjdev.desktop.extensions.CustomExt.jsonToList
import org.mjdev.desktop.extensions.PathExt.dirsOnly
import org.mjdev.desktop.extensions.PathExt.filesOnly
import org.mjdev.desktop.extensions.PathExt.get
import org.mjdev.desktop.extensions.PathExt.lines
import org.mjdev.desktop.extensions.PathExt.sortedByName
import org.mjdev.desktop.extensions.PathExt.text
import org.mjdev.desktop.helpers.system.shell.Shell
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.interfaces.ILocale
import java.util.Locale

@Suppress("unused", "MemberVisibilityCanBePrivate")
class AppsManager(
    val context: IDesktopContext,
    val scope: CoroutineScope = context.scope,
) : IAppsManager {
    companion object {
        const val DIR_NAME_ROOT = "/"
        const val DIR_NAME_USR = "/usr"
        const val DIR_NAME_VAR = "/var"
        const val DIR_NAME_DOT_CONFIG = ".config"
        const val DIR_NAME_DOT_ICONS = ".icons"
        const val DIR_NAME_DOT_THEMES = ".themes"
        const val DIR_NAME_DOT_LOCAL = ".local"
        const val DIR_NAME_SHARE = "share"
        const val DIR_NAME_APPLICATIONS = "applications"
        const val DIR_NAME_AUTOSTART = "autoStart"
        const val DIR_NAME_BACKGROUNDS = "backgrounds"
        const val DIR_NAME_MENUS = "menus"
        const val DIR_NAME_LIB = "lib"
        const val DIR_NAME_FLATPAK = "flatpak"
        const val DIR_NAME_EXPORTS = "exports"
        const val DIR_NAME_SNAPD = "snapd"
        const val DIR_NAME_DESKTOP = "desktop"

        const val FILE_NAME_GNOME_APPS_MENU = "gnome-applications.menu"
        const val FILE_NAME_BACKGROUND = "background"
        const val FILE_NAME_MIME_APPS_LIST = "mimeapps.list"
        const val FILE_NAME_USER_DIRS = "user-dirs.dirs"
        const val FILE_NAME_USER_DIRS_LOCALE = "user-dirs.locale"
    }

    private val currentUser
        get() = context.currentUser

    private val homeDir
        get() = currentUser.homeDir

    private val rootDir: Path = DIR_NAME_ROOT.toPath()

    private val usrDir: Path = DIR_NAME_USR.toPath()

    private val varDir: Path = DIR_NAME_VAR.toPath()

    private val configDir: Path = homeDir[DIR_NAME_DOT_CONFIG]

    private val iconsDir: Path = homeDir[DIR_NAME_DOT_ICONS]

    private val themesDir: Path = homeDir[DIR_NAME_DOT_THEMES]

    private val localDir: Path = homeDir[DIR_NAME_DOT_LOCAL]

    private val localShareDir: Path = localDir[DIR_NAME_SHARE]

    private val allAppsDesktopFilesDir: Path = localShareDir[DIR_NAME_APPLICATIONS]

    private val autostartDesktopFilesDir: Path = configDir[DIR_NAME_AUTOSTART]

    private val backgroundFilesDir: Path by lazy { localShareDir[DIR_NAME_BACKGROUNDS] }
    // private val userBackgroundFilesDir by lazy { homeDir?.resolve("pictures") } // todo

    private val menusDir: Path = configDir[DIR_NAME_MENUS]

    private val gnomeAppsFile: Path = menusDir[FILE_NAME_GNOME_APPS_MENU] // xml

    private val backgroundFile: Path = configDir[FILE_NAME_BACKGROUND] // jpg

    private val mimeApps by lazy {
        configDir[FILE_NAME_MIME_APPS_LIST].lines
    } // <mime>=<desktop.file>

    private val userDirs by lazy {
        configDir[FILE_NAME_USER_DIRS].text
    } // <type>="<path>"

    private val userDirsLocale by lazy {
        configDir[FILE_NAME_USER_DIRS_LOCALE].textAsLocale
    } // SK_sk

    private val menusItems by lazy {
        menusDir[FILE_NAME_GNOME_APPS_MENU].text
    } // xml

    private val iconThemes by lazy { iconsDir.dirsOnly } // folders

    private val systemThemes by lazy { themesDir.dirsOnly } // folders

    private val autoStartDesktopFiles by lazy {
        autostartDesktopFilesDir.desktopFiles
    }

    // todo : probably not all here, need usr and local also
    private val allAppsDesktopFilesLocal by lazy {
        allAppsDesktopFilesDir.desktopFiles
    }

    private val allAppsDesktopFilesShared by lazy {
        usrDir[DIR_NAME_SHARE][DIR_NAME_APPLICATIONS].desktopFiles
    }

    private val allAppsDesktopFilesFlatPack by lazy {
        varDir[DIR_NAME_LIB][DIR_NAME_FLATPAK][DIR_NAME_EXPORTS][DIR_NAME_SHARE][DIR_NAME_APPLICATIONS].desktopFiles
    }

    private val allAppsDesktopFilesSnap by lazy {
        varDir[DIR_NAME_LIB][DIR_NAME_SNAPD][DIR_NAME_DESKTOP][DIR_NAME_APPLICATIONS].desktopFiles
    }

    private val allAppsDesktopFiles by lazy {
        (
            allAppsDesktopFilesLocal +
                allAppsDesktopFilesShared +
                allAppsDesktopFilesFlatPack +
                allAppsDesktopFilesSnap
        ).distinctBy { it.file.name }
    }

    override val currentLocale: ILocale
        get() = userDirsLocale.toILocale()

    val autoStartApps
        get() = autoStartDesktopFiles.map { file -> App(file) }

    override val allApps
        get() = allAppsDesktopFiles.map { file -> App(file) }

    val backgrounds by lazy {
        backgroundFilesDir.filesOnly.sortedByName()
    }

    override val categories
        get() =
            allApps
                .asSequence()
                .flatMap { app ->
                    app.categories
                }.distinct()
                .toList()
                .sortedBy { c ->
                    c.name
                }.sortedByDescending { c ->
                    c.priority
                }.toList()

    override val favoriteApps: List<App>
        get() =
            Shell
                .executeAndRead(
                    "gsettings",
                    "get",
                    "org.gnome.shell",
                    "favorite-apps",
                ).replace("'", "\"") // todo ?
                .jsonToList<String>()
                .flatMap { deskFileName ->
                    findDesktopFileByName(deskFileName).map { deskFile ->
                        App(deskFile)
                    }
                }

    fun findDesktopFileByName(deskFileName: String) =
        allAppsDesktopFiles.filter { deskFile ->
            deskFile.fileName.contentEquals(deskFileName)
        }

    override suspend fun startApp(app: IApp) {
        app.start()
    }

    // todo languages
//    fun provideCategory(
//        name: String
//    ): Category {
//        return name.trim().let { n ->
//            if (n.isEmpty() || n.startsWith("X-", true)) Category.Empty
//            else Category(name)
//        }
//    }
// }
}

@Suppress("UnusedReceiverParameter")
private fun Locale.toILocale(): ILocale {
    return ILocale.DEFAULT // todo
}

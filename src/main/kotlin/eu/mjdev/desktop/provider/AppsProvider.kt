package eu.mjdev.desktop.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.data.Category
import eu.mjdev.desktop.extensions.Custom.desktopFiles
import eu.mjdev.desktop.extensions.Custom.dirsOnly
import eu.mjdev.desktop.extensions.Custom.filesOnly
import eu.mjdev.desktop.extensions.Custom.flowBlock
import eu.mjdev.desktop.extensions.Custom.get
import eu.mjdev.desktop.extensions.Custom.jsonToList
import eu.mjdev.desktop.extensions.Custom.lines
import eu.mjdev.desktop.extensions.Custom.sortedByName
import eu.mjdev.desktop.extensions.Custom.text
import eu.mjdev.desktop.extensions.Custom.textAsLocale
import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
class AppsProvider(
    val api: DesktopProvider,
    val scope: CoroutineScope = api.scope
) {
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

        // todo
        @Composable
        fun rememberFavoriteApps(
            api: DesktopProvider = LocalDesktop.current,
            appsProvider: AppsProvider = api.appsProvider,
        ): State<List<App>> {
//            var processes by mutableStateOf(0)
//            processManagerListener { processes = size }
            return appsProvider.favoriteApps.collectAsState(emptyList())
            // todo

        }
    }

    private val rootDir = File(DIR_NAME_ROOT)
    private val usrDir = File(DIR_NAME_USR)
    private val varDir = File(DIR_NAME_VAR)

    private val configDir = api.homeDir[DIR_NAME_DOT_CONFIG]
    private val iconsDir = api.homeDir[DIR_NAME_DOT_ICONS]
    private val themesDir = api.homeDir[DIR_NAME_DOT_THEMES]
    private val localDir = api.homeDir[DIR_NAME_DOT_LOCAL]
    private val localShareDir = localDir[DIR_NAME_SHARE]
    private val allAppsDesktopFilesDir = localShareDir[DIR_NAME_APPLICATIONS]
    private val autostartDesktopFilesDir = configDir[DIR_NAME_AUTOSTART]

    private val backgroundFilesDir by lazy { localShareDir[DIR_NAME_BACKGROUNDS] }
    // private val userBackgroundFilesDir by lazy { homeDir?.resolve("pictures") } // todo

    private val menusDir = configDir[DIR_NAME_MENUS]
    private val gnomeAppsFile = menusDir[FILE_NAME_GNOME_APPS_MENU] // xml
    private val backgroundFile = configDir[FILE_NAME_BACKGROUND] // jpg

    private val mimeApps by lazy {
        configDir[FILE_NAME_MIME_APPS_LIST].lines
    } // <mime>=<desktop.file>
    private val userDirs by lazy {
        configDir[FILE_NAME_USER_DIRS].text
    } // <type>="<path>"
    private val userDirsLocale by lazy {
        configDir[FILE_NAME_USER_DIRS_LOCALE].textAsLocale
    } //SK_sk

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
        (allAppsDesktopFilesLocal +
                allAppsDesktopFilesShared +
                allAppsDesktopFilesFlatPack +
                allAppsDesktopFilesSnap).distinctBy { it.file.name }
    }

    val currentLocale: Locale
        get() = userDirsLocale
    val autoStartApps
        get() = autoStartDesktopFiles.map { file -> App(file) }
    val allApps
        get() = allAppsDesktopFiles.map { file -> App(file) }

    val backgrounds by lazy {
        backgroundFilesDir.filesOnly.sortedByName()
    }

    val appCategories = flowBlock {
        allApps.asSequence().flatMap { app ->
            app.categories.map { c -> provideCategory(c) }
        }.distinct().sorted().map { c ->
            Category(c)
        }.sortedByDescending { c ->
            c.priority
        }.toList()
    }.flowOn(Dispatchers.IO)

    val favoriteApps: Flow<List<App>> = flowBlock {
        Shell.executeAndRead(
            "gsettings",
            "get",
            "org.gnome.shell",
            "favorite-apps"
        ).jsonToList<String>().asSequence().flatMap { deskFileName ->
            allAppsDesktopFiles.filter { deskFile ->
                deskFile.fileName.contentEquals(deskFileName)
            }.map { deskFile ->
                App(deskFile)
            }
        }.toList()
    }.flowOn(Dispatchers.IO)

    // todo languages
    fun provideCategory(name: String): String {
        return name.trim().let { n ->
            if (n.isEmpty() || n.startsWith("X-", true)) Category.UNCATEGORIZED
            else name
        }
    }
}

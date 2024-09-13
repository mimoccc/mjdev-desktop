package eu.mjdev.desktop.provider.providers

import eu.mjdev.desktop.extensions.Locale.toLocale
import eu.mjdev.desktop.helpers.Command
import eu.mjdev.desktop.helpers.Command.Companion.toList
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.data.App
import eu.mjdev.desktop.provider.data.Category
import eu.mjdev.desktop.provider.data.DesktopFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
class AppsProvider(
    val desktopProvider: DesktopProvider
) {
    val homeDir by lazy { runCatching { File(System.getProperty("user.home")) }.getOrNull() }

    private val configDir by lazy { homeDir?.resolve(".config") }
    private val iconsDir by lazy { homeDir?.resolve(".icons") }
    private val themesDir by lazy { homeDir?.resolve(".themes") }
    private val localDir by lazy { homeDir?.resolve(".local") }
    private val localShareDir by lazy { localDir?.resolve("share") }
    private val allAppsDesktopFilesDir by lazy { localShareDir?.resolve("applications") }
    private val autostartDesktopFilesDir by lazy { configDir?.resolve("autoStart") }
    private val backgroundFilesDir by lazy { localShareDir?.resolve("backgrounds") }

    private val menusDir by lazy { configDir?.resolve("menus") }

    private val gnomeAppsFile by lazy { menusDir?.resolve("gnome-applications.menu") } // xml
    private val backgroundFile by lazy { configDir?.resolve("background") } // jpg

    private val mimeApps by lazy {
        runCatching {
            configDir?.resolve("mimeapps.list")?.readLines()
        }.getOrNull()
    } // <mime>=<desktop.file>
    private val userDirs by lazy {
        runCatching {
            configDir?.resolve("user-dirs.dirs")?.readText()
        }.getOrNull()
    } // <type>="<path>"
    private val userDirsLocale by lazy {
        runCatching {
            configDir?.resolve("user-dirs.locale")?.readText()?.toLocale()
        }.getOrNull()
    } //SK_sk

    private val menusItems by lazy {
        runCatching {
            menusDir?.resolve("gnome-applications.menu")?.readText()
        }.getOrNull()
    } // xml

    private val iconThemes by lazy { runCatching { iconsDir?.listFiles() }.getOrNull() } // folders
    private val systemThemes by lazy { runCatching { themesDir?.listFiles() }.getOrNull() } // folders

    private val autoStartDesktopFiles
        get() = runCatching {
            autostartDesktopFilesDir?.listFiles()?.map { DesktopFile(it) }
        }.getOrNull() ?: emptyList()

    // todo : probably not all here, need usr and local also
    private val allAppsDesktopFilesLocal
        get() = runCatching {
            allAppsDesktopFilesDir?.listFiles()?.filter {
                it.extension == "desktop"
            }?.map { DesktopFile(it) }
        }.getOrNull() ?: emptyList()

    private val allAppsDesktopFilesShared
        get() = runCatching {
            File("/usr/share/applications").listFiles()?.filter {
                it.extension == "desktop"
            }?.map { DesktopFile(it) }
        }.getOrNull() ?: emptyList()

    private val allAppsDesktopFilesFlatPack
        get() = runCatching {
            File("/var/lib/flatpak/exports/share/applications/").listFiles()?.filter {
                it.extension == "desktop"
            }?.map { DesktopFile(it) }
        }.getOrNull() ?: emptyList()

    private val allAppsDesktopFilesSnap
        get() = runCatching {
            File("/var/lib/snapd/desktop/applications/").listFiles()?.filter {
                it.extension == "desktop"
            }?.map { DesktopFile(it) }
        }.getOrNull() ?: emptyList()

    private val allAppsDesktopFiles
        get() = (allAppsDesktopFilesLocal +
                allAppsDesktopFilesShared +
                allAppsDesktopFilesFlatPack +
                allAppsDesktopFilesSnap).distinctBy { it.file.name }

    val currentLocale: Locale
        get() = userDirsLocale ?: Locale.ENGLISH
    val autoStartApps
        get() = autoStartDesktopFiles.map { file -> App(desktopFile = file, file = file.file) }
    val allApps
        get() = allAppsDesktopFiles.map { file -> App(desktopFile = file, file = file.file) }
    val backgrounds
        get() = runCatching { backgroundFilesDir?.listFiles()?.toList() }.getOrNull() ?: emptyList<File>()
    val appCategories
        get() = mutableListOf<String>().apply {
            allApps.forEach { app ->
                addAll(app.categories)
            }
        }.distinct().sorted().map { Category(it) }
    val categoriesAndApps
        get() = mutableMapOf<String, List<App>>().let { map ->
            allApps.forEach { app ->
                app.categories.forEach { category ->
                    if (!map.containsKey(category)) {
                        map[category] = mutableListOf()
                    }
                    (map[category] as MutableList<App>).add(app)
                }
            }
            map
        }

    val favoriteApps: Flow<List<App>>
        get() = flow {
            Command(
                "gsettings",
                "get",
                "org.gnome.shell",
                "favorite-apps"
            ).execute()?.toList<String>()?.map { deskFileName ->
                allAppsDesktopFiles.filter { deskFile ->
                    deskFile.file.name.contentEquals(deskFileName) == true
                }.map { deskFile ->
                    App(desktopFile = deskFile, file = deskFile.file)
                }.firstOrNull()
            }.also { list ->
                emit(list?.filterNotNull() ?: emptyList())
            }
        }

}

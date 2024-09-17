package eu.mjdev.desktop.provider

import androidx.compose.runtime.mutableStateListOf
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.data.Category
import eu.mjdev.desktop.data.DesktopFile
import eu.mjdev.desktop.extensions.Custom.invalidate
import eu.mjdev.desktop.extensions.Locale.toLocale
import eu.mjdev.desktop.helpers.exception.EmptyException.Companion.EmptyException
import eu.mjdev.desktop.helpers.system.Command
import eu.mjdev.desktop.helpers.system.Command.Companion.toList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate", "UNNECESSARY_SAFE_CALL")
class AppsProvider(
    val api: DesktopProvider
) {
    val scope
        get() = api.scope

    val homeDir by lazy { runCatching { File(System.getProperty("user.home")) }.getOrNull() }

    private val configDir by lazy { homeDir?.resolve(".config") }
    private val iconsDir by lazy { homeDir?.resolve(".icons") }
    private val themesDir by lazy { homeDir?.resolve(".themes") }
    private val localDir by lazy { homeDir?.resolve(".local") }
    private val localShareDir by lazy { localDir?.resolve("share") }
    private val allAppsDesktopFilesDir by lazy { localShareDir?.resolve("applications") }
    private val autostartDesktopFilesDir by lazy { configDir?.resolve("autoStart") }
    private val backgroundFilesDir by lazy { localShareDir?.resolve("backgrounds") }
    // todo
//    private val userBackgroundFilesDir by lazy { homeDir?.resolve("pictures") }

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

    private val autoStartDesktopFiles by lazy {
        runCatching {
            autostartDesktopFilesDir?.listFiles()?.map { DesktopFile(it) }
        }.getOrNull() ?: emptyList()
    }

    // todo : probably not all here, need usr and local also
    private val allAppsDesktopFilesLocal by lazy {
        runCatching {
            allAppsDesktopFilesDir?.listFiles()?.filter {
                it.extension == "desktop"
            }?.map { DesktopFile(it) }
        }.getOrNull() ?: emptyList()
    }

    private val allAppsDesktopFilesShared by lazy {
        runCatching {
            File("/usr/share/applications").listFiles()?.filter {
                it.extension == "desktop"
            }?.map { DesktopFile(it) }
        }.getOrNull() ?: emptyList()
    }

    private val allAppsDesktopFilesFlatPack by lazy {
        runCatching {
            File("/var/lib/flatpak/exports/share/applications/").listFiles()?.filter {
                it.extension == "desktop"
            }?.map { DesktopFile(it) }
        }.getOrNull() ?: emptyList()
    }

    private val allAppsDesktopFilesSnap by lazy {
        runCatching {
            File("/var/lib/snapd/desktop/applications/").listFiles()?.filter {
                it.extension == "desktop"
            }?.map { DesktopFile(it) }
        }.getOrNull() ?: emptyList()
    }

    private val allAppsDesktopFiles by lazy {
        (allAppsDesktopFilesLocal +
                allAppsDesktopFilesShared +
                allAppsDesktopFilesFlatPack +
                allAppsDesktopFilesSnap).distinctBy { it.file.name }
    }

    val currentLocale: Locale
        get() = userDirsLocale ?: Locale.ENGLISH
    val autoStartApps
        get() = autoStartDesktopFiles.map { file -> App(desktopFile = file, file = file.file) }
    val allApps
        get() = allAppsDesktopFiles.map { file -> App(desktopFile = file, file = file.file) }
    val backgrounds
        get() = runCatching {
            backgroundFilesDir?.listFiles()?.toList()?.sortedBy { it.name }?.filter { it.isFile }
        }.getOrNull() ?: emptyList<File>()
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

    val favoriteApps = mutableStateListOf<App>()

    fun startApp(app: App) {
        scope.launch(Dispatchers.IO) {
            if (app.isRunning) {
                // todo menu to switch
                if (app.isWindowFocus(api)) {
                    app.minimizeWindow(api)
                } else {
                    app.requestWindowFocus(api)
                }
            } else {
                app.onStarting {
                    favoriteApps.invalidate()
                }.onStarted {
                    favoriteApps.invalidate()
                }.onStop { result ->
                    favoriteApps.invalidate()
                    if (result != EmptyException) {
                        result.printStackTrace()
                    }
                }.start()
                favoriteApps.invalidate()
            }
        }
    }

    init {
        Command(
            "gsettings",
            "get",
            "org.gnome.shell",
            "favorite-apps"
        ).execute()
            ?.toList<String>()
            ?.mapNotNull { deskFileName ->
                allAppsDesktopFiles.filter { deskFile ->
                    deskFile.file.name?.contentEquals(deskFileName) == true
                }.map { deskFile ->
                    App(
                        desktopFile = deskFile,
                        file = deskFile.file
                    )
                }.firstOrNull()
            }?.also { favorite ->
                favoriteApps.addAll(favorite)
            }
    }

}

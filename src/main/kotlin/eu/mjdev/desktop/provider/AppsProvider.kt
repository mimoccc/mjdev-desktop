package eu.mjdev.desktop.provider

import com.google.gson.Gson
import eu.mjdev.desktop.provider.data.App
import eu.mjdev.desktop.provider.data.Category
import kotlinx.coroutines.flow.flow
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.io.File
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
class AppsProvider(
    val desktopProvider: DesktopProvider
) {
    val homeDir by lazy { File(System.getProperty("user.home")) }

    private val configDir by lazy { homeDir.resolve(".config") }
    private val iconsDir by lazy { homeDir.resolve(".icons") }
    private val themesDir by lazy { homeDir.resolve(".themes") }
    private val localDir by lazy { homeDir.resolve(".local") }
    private val localShareDir by lazy { localDir.resolve("share") }
    private val allAppsDesktopFilesDir by lazy { localShareDir.resolve("applications") }
    private val autostartDesktopFilesDir by lazy { configDir.resolve("autoStart") }
    private val backgroundFilesDir by lazy { localShareDir.resolve("backgrounds") }

    private val menusDir by lazy { configDir.resolve("menus") }

    private val gnomeAppsFile by lazy { menusDir.resolve("gnome-applications.menu") } // xml
    private val backgroundFile by lazy { configDir.resolve("background") } // jpg

    private val mimeApps by lazy { configDir.resolve("mimeapps.list").readLines() } // <mime>=<desktop.file>
    private val userDirs by lazy { configDir.resolve("user-dirs.dirs").readText() } // <type>="<path>"
    private val userDirsLocale by lazy { configDir.resolve("user-dirs.locale").readText().toLocale() } //SK_sk

    private val menusItems by lazy { menusDir.resolve("gnome-applications.menu").readText() } // xml

    private val iconThemes by lazy { iconsDir.listFiles() } // folders
    private val systemThemes by lazy { themesDir.listFiles() } // folders

    private val autoStartDesktopFiles: List<File>
        get() = autostartDesktopFilesDir.listFiles()?.toList() ?: emptyList() // List<DesktopFile>
    private val allAppsDesktopFiles: List<File>
        get() = allAppsDesktopFilesDir.listFiles()?.toList() ?: emptyList() // List<DesktopFile>

    val currentLocale: Locale
        get() = userDirsLocale
    val autoStartApps
        get() = autoStartDesktopFiles.map { file -> App(file) }
    val allApps
        get() = allAppsDesktopFiles.map { file -> App(file) }
    val backgrounds
        get() = backgroundFilesDir.listFiles()?.toList() ?: emptyList<File>()
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

    val favoriteApps
        get() = flow {
            desktopProvider.runCommandForOutput(
                "gsettings",
                "get",
                "org.gnome.shell",
                "favorite-apps"
            ).let { json ->
                Gson().fromJson(json, List::class.java)
            }.mapNotNull { deskFileName ->
                allAppsDesktopFiles.firstOrNull { deskFile ->
                    @Suppress("UNNECESSARY_SAFE_CALL")
                    deskFile?.name?.contentEquals(deskFileName.toString()) == true
                }.let {
                    App(it)
                }
            }.also {
                emit(it)
            }
        }

    fun iconForApp(
        name: String?,
    ): Int? = runCatching {
        if (name != null) {
            desktopProvider.currentUser.theme.iconSet.codePointsFile.icons.map { icon ->
                Pair(FuzzySearch.ratio(name, icon.key), icon)
            }.maxByOrNull { it.first }?.second?.value
        } else null
    }.getOrNull()

}

private fun String.toLocale() = this.split("_").let { lc ->
    when (lc.size) {
        2 -> Locale(lc[0], lc[1])
        1 -> Locale(lc[0])
        else -> Locale.ENGLISH
    }
}

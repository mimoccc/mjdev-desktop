package eu.mjdev.desktop.helpers.system

import eu.mjdev.desktop.provider.DesktopProvider
import java.io.File

@Suppress("unused")
class UserDirs(
    api: DesktopProvider,
    homeDir: File = api.homeDir,
    configFile: File = File(api.homeDir, "/.config/user-dirs.dirs"),
    configFileContent: List<String> = configFile.readLines().filter {
        (!it.startsWith("#")) && (!it.trim().isEmpty())
    }.map {
        it.replace("\$HOME", homeDir.absolutePath)
            .replace("\"", "")
    }
) : HashMap<String, File>() {
    init {
        configFileContent.map {
            it.split("=").let { Pair(it[0], it.getOrNull(1)) }
        }.forEach {
            put(it.first, File(it.second ?: ""))
        }
    }

    val desktopDirectory
        get() = this[XDG_DESKTOP_DIR]
    val downloadDirectory
        get() = this[XDG_DOWNLOAD_DIR]
    val templatesDirectory
        get() = this[XDG_TEMPLATES_DIR]
    val publicShareDirectory
        get() = this[XDG_PUBLICSHARE_DIR]
    val documentsDirectory
        get() = this[XDG_DOCUMENTS_DIR]
    val musicDirectory
        get() = this[XDG_MUSIC_DIR]
    val picturesDirectory
        get() = this[XDG_PICTURES_DIR]
    val videosDirectory
        get() = this[XDG_VIDEOS_DIR]

    companion object {
        const val XDG_DESKTOP_DIR = "XDG_DESKTOP_DIR"
        const val XDG_DOWNLOAD_DIR = "XDG_DOWNLOAD_DIR"
        const val XDG_TEMPLATES_DIR = "XDG_TEMPLATES_DIR"
        const val XDG_PUBLICSHARE_DIR = "XDG_PUBLICSHARE_DIR"
        const val XDG_DOCUMENTS_DIR = "XDG_DOCUMENTS_DIR"
        const val XDG_MUSIC_DIR = "XDG_MUSIC_DIR"
        const val XDG_PICTURES_DIR = "XDG_PICTURES_DIR"
        const val XDG_VIDEOS_DIR = "XDG_VIDEOS_DIR"
    }
}
package eu.mjdev.desktop.helpers.system

import eu.mjdev.desktop.extensions.Custom.lines
import eu.mjdev.desktop.extensions.Custom.notStartsWith
import java.io.File

// todo api has directories & hashmap from file
@Suppress("unused")
class UserDirs(
    homeDir: File,
    configFile: File = File(homeDir, "/.config/user-dirs.dirs"),
    configFileContent: List<String> =
        configFile.lines.filter { line ->
            line.let { l -> l.isNotEmpty() && l.notStartsWith("#") }
        }.map { s ->
            s.replace("\$HOME", homeDir.absolutePath).replace("\"", "")
        }
) : HashMap<String, File>() {
    init {
        configFileContent.map { line ->
            line.split("=").let { pair -> Pair(pair[0], pair.getOrNull(1)) }
        }.forEach {
            put(it.first, File(it.second.orEmpty()))
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

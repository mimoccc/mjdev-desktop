package org.mjdev.desktop.data

import okio.Path
import okio.Path.Companion.toPath
import org.mjdev.desktop.extensions.PathExt.absolutePath
import org.mjdev.desktop.extensions.PathExt.get
import org.mjdev.desktop.extensions.PathExt.lines
import org.mjdev.desktop.extensions.Text.notStartsWith
import org.mjdev.desktop.interfaces.IUserDirs

// todo api has directories & hashmap from file
@Suppress("unused")
class UserDirs(
    override val homeDirectory: Path,
) : IUserDirs {
    private val configFile: Path by lazy {
        homeDirectory.resolve(".config").resolve("user-dirs.dirs")
    }
    private val configFileContent: List<String> by lazy {
        configFile.lines
            .filter { line ->
                line.let { l -> l.isNotEmpty() && l.notStartsWith("#") }
            }.map { s ->
                s
                    .replace(
                        "\$HOME",
                        homeDirectory.absolutePath,
                    ).replace("\"", "")
            }
    }
    val data by lazy {
        configFileContent
            .map { line ->
                line.split("=").let { pair ->
                    Pair(pair[0], pair.getOrNull(1))
                }
            }.associate {
                Pair(it.first, it.second.orEmpty())
            }
    }

    operator fun get(name: String): Path = data[name].toString().toPath()

    private val localDir
        get() = homeDirectory[DIR_NAME_DOT_LOCAL]
    private val localShareDir
        get() = localDir[DIR_NAME_SHARE]

    override val desktopDirectory: Path
        get() = this[XDG_DESKTOP_DIR]
    override val downloadDirectory: Path
        get() = this[XDG_DOWNLOAD_DIR]
    override val templatesDirectory: Path
        get() = this[XDG_TEMPLATES_DIR]
    override val publicShareDirectory
        get() = this[XDG_PUBLICSHARE_DIR]
    override val documentsDirectory
        get() = this[XDG_DOCUMENTS_DIR]
    override val musicDirectory
        get() = this[XDG_MUSIC_DIR]
    override val picturesDirectory
        get() = this[XDG_PICTURES_DIR]
    override val videosDirectory
        get() = this[XDG_VIDEOS_DIR]
    override val backgroundsDirectory: Path
        get() = localShareDir[DIR_NAME_BACKGROUNDS]

    companion object {
        const val XDG_DESKTOP_DIR = "XDG_DESKTOP_DIR"
        const val XDG_DOWNLOAD_DIR = "XDG_DOWNLOAD_DIR"
        const val XDG_TEMPLATES_DIR = "XDG_TEMPLATES_DIR"
        const val XDG_PUBLICSHARE_DIR = "XDG_PUBLICSHARE_DIR"
        const val XDG_DOCUMENTS_DIR = "XDG_DOCUMENTS_DIR"
        const val XDG_MUSIC_DIR = "XDG_MUSIC_DIR"
        const val XDG_PICTURES_DIR = "XDG_PICTURES_DIR"
        const val XDG_VIDEOS_DIR = "XDG_VIDEOS_DIR"

        const val DIR_NAME_SHARE = "share"
        const val DIR_NAME_DOT_LOCAL = ".local"
        const val DIR_NAME_BACKGROUNDS = "backgrounds"
    }
}

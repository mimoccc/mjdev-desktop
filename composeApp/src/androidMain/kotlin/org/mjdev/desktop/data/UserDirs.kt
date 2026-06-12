package org.mjdev.desktop.data

import okio.Path
import org.mjdev.desktop.extensions.PathExt.mkdirs
import org.mjdev.desktop.interfaces.IUserDirs

@Suppress("unused")
class UserDirs(
    override val homeDirectory: Path
) : IUserDirs {

    operator fun get(
        name: String
    ): Path = homeDirectory.resolve(name)

    init {
        runCatching { desktopDirectory.mkdirs() }
        runCatching { downloadDirectory.mkdirs() }
        runCatching { templatesDirectory.mkdirs() }
        runCatching { publicShareDirectory.mkdirs() }
        runCatching { documentsDirectory.mkdirs() }
        runCatching { musicDirectory.mkdirs() }
        runCatching { picturesDirectory.mkdirs() }
        runCatching { videosDirectory.mkdirs() }
        runCatching { backgroundsDirectory.mkdirs() }
    }

    override val desktopDirectory: Path
        get() = this[IUserDirs.DESKTOP_DIR]
    override val downloadDirectory: Path
        get() = this[IUserDirs.DOWNLOAD_DIR]
    override val templatesDirectory: Path
        get() = this[IUserDirs.TEMPLATES_DIR]
    override val publicShareDirectory
        get() = this[IUserDirs.PUBLICSHARE_DIR]
    override val documentsDirectory
        get() = this[IUserDirs.DOCUMENTS_DIR]
    override val musicDirectory
        get() = this[IUserDirs.MUSIC_DIR]
    override val picturesDirectory
        get() = this[IUserDirs.PICTURES_DIR]
    override val videosDirectory
        get() = this[IUserDirs.VIDEOS_DIR]
    override val backgroundsDirectory: Path
        get() = this[IUserDirs.BACKGROUNDS_DIR]
}
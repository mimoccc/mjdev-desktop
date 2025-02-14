package org.mjdev.desktop.data

import okio.Path
import org.mjdev.desktop.interfaces.IUserDirs

@Suppress("unused")
class UserDirs(
    override val homeDirectory: Path
) : IUserDirs {

    operator fun get(
        name: String
    ): Path = homeDirectory.resolve(name)

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
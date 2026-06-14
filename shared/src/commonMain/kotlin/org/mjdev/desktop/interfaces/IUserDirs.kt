package org.mjdev.desktop.interfaces

import okio.Path
import okio.Path.Companion.toPath

interface IUserDirs {
    val homeDirectory: Path
    val desktopDirectory: Path
    val downloadDirectory: Path
    val templatesDirectory: Path
    val publicShareDirectory: Path
    val documentsDirectory: Path
    val musicDirectory: Path
    val picturesDirectory: Path
    val videosDirectory: Path
    val backgroundsDirectory: Path

    companion object {
        const val DESKTOP_DIR = "Desktop"
        const val DOWNLOAD_DIR = "Download"
        const val TEMPLATES_DIR = "Templates"
        const val PUBLICSHARE_DIR = "Shared"
        const val DOCUMENTS_DIR = "Documents"
        const val MUSIC_DIR = "Music"
        const val PICTURES_DIR = "Pictures"
        const val VIDEOS_DIR = "Videos"
        const val BACKGROUNDS_DIR = "Backgrounds"

        val DEFAULT =
            object : IUserDirs {
                override val homeDirectory: Path = "~".toPath()
                override val desktopDirectory: Path = homeDirectory.resolve(DESKTOP_DIR)
                override val downloadDirectory: Path = homeDirectory.resolve(DOWNLOAD_DIR)
                override val templatesDirectory: Path = homeDirectory.resolve(TEMPLATES_DIR)
                override val publicShareDirectory: Path = homeDirectory.resolve(PUBLICSHARE_DIR)
                override val documentsDirectory: Path = homeDirectory.resolve(DOCUMENTS_DIR)
                override val musicDirectory: Path = homeDirectory.resolve(MUSIC_DIR)
                override val picturesDirectory: Path = homeDirectory.resolve(PICTURES_DIR)
                override val videosDirectory: Path = homeDirectory.resolve(VIDEOS_DIR)
                override val backgroundsDirectory: Path = homeDirectory.resolve(BACKGROUNDS_DIR)
            }
    }
}

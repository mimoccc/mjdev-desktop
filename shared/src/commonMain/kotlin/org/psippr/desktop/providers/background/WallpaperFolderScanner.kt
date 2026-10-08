package org.psippr.desktop.providers.background

import okio.Path
import org.psippr.desktop.extensions.PathExt.absolutePath
import org.psippr.desktop.extensions.PathExt.dirsOnly
import org.psippr.desktop.extensions.PathExt.extension
import org.psippr.desktop.extensions.PathExt.filesOnly
import org.psippr.desktop.extensions.PathExt.get
import org.psippr.desktop.extensions.PathExt.sortedByName
import org.psippr.desktop.interfaces.IUserDirs

/**
 * Finds wallpaper folders on disk. The roots are the standard Linux locations; the "Pictures"
 * root is resolved through the XDG user dirs, so it follows the system language
 * (e.g. `~/Obrázky/Backgrounds`). Hidden folders (starting with a dot) are ignored.
 */
object WallpaperFolderScanner {
    private const val HIDDEN_PREFIX = "."
    private const val LOCAL_DIR = ".local"
    private const val SHARE_DIR = "share"
    private const val BACKGROUNDS_LOWER = "backgrounds"
    private const val BACKGROUNDS = "Backgrounds"
    private const val IMAGES_DIR = "Images"

    /** Wallpaper root folders for the given [dirs], without duplicates. */
    fun roots(dirs: IUserDirs): List<Path> {
        val home = dirs.homeDirectory
        return listOf(
            home[LOCAL_DIR][SHARE_DIR][BACKGROUNDS_LOWER],
            home[BACKGROUNDS_LOWER],
            home[IUserDirs.PICTURES_DIR][BACKGROUNDS],
            dirs.picturesDirectory[BACKGROUNDS],
            home[IMAGES_DIR][BACKGROUNDS],
        ).distinctBy { it.absolutePath }
    }

    /** Non-hidden sub-folders of all roots of [dirs] that exist on disk. */
    fun folders(dirs: IUserDirs): List<WallpaperFolder> = roots(dirs)
        .flatMap { root -> root.dirsOnly.sortedByName() }
        .filter { dir -> !dir.name.startsWith(HIDDEN_PREFIX) }
        .distinctBy { it.absolutePath }
        .map { dir -> dir.toFolder() }

    /** Supported image files placed directly in [root] (not in sub-folders). */
    fun looseImages(root: Path): List<Path> = root.images()

    private fun Path.toFolder() = WallpaperFolder(
        path = this,
        name = name,
        key = absolutePath,
        images = images(),
    )

    private fun Path.images(): List<Path> = filesOnly
        .filter { file -> WallpaperImageExtensions.isSupported(file.extension) }
        .sortedByName()
}

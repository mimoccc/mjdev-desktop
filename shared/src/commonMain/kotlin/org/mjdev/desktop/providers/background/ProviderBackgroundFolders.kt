package org.mjdev.desktop.providers.background

import org.mjdev.desktop.helpers.compose.ImagesProvider
import org.mjdev.desktop.interfaces.IUser

/**
 * Local wallpapers from every wallpaper root: images lying directly in a root plus images of
 * each sub-folder that is not listed in [excludedFolders] (keys of [WallpaperFolder]).
 * Replaces [ProviderLocal], which only knew a single folder.
 */
class ProviderBackgroundFolders(
    private val user: IUser,
    private val excludedFolders: Set<String> = emptySet(),
) : ImagesProvider {
    private val files
        get() =
            WallpaperFolderScanner.roots(user.userDirs).flatMap { root -> WallpaperFolderScanner.looseImages(root) } +
                WallpaperFolderScanner
                    .folders(user.userDirs)
                    .filter { folder -> folder.key !in excludedFolders }
                    .flatMap { folder -> folder.images }

    override val size: Int
        get() = files.size

    override suspend fun get(): Any = files
}

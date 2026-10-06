package org.mjdev.desktop.providers.background

import okio.Path

/**
 * A single wallpaper sub-folder found under one of the wallpaper roots.
 *
 * @property path folder location
 * @property name folder name only (no full path), used as the list label
 * @property key stable identifier persisted when the folder is excluded
 * @property images wallpaper images directly inside the folder, sorted by name
 */
data class WallpaperFolder(
    val path: Path,
    val name: String,
    val key: String,
    val images: List<Path>,
)

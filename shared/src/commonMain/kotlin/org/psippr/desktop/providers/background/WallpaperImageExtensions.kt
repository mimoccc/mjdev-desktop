package org.psippr.desktop.providers.background

/** File extensions (lower case, without dot) that are treated as wallpaper images. */
object WallpaperImageExtensions {
    /** All supported wallpaper extensions. */
    val SUPPORTED: Set<String> = setOf("jpg", "jpeg", "png", "webp", "bmp", "gif", "svg")

    /** Whether the given [extension] (any case) is a supported wallpaper image extension. */
    fun isSupported(extension: String): Boolean = extension.lowercase() in SUPPORTED
}

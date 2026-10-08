package org.psippr.desktop.components.controlcenter.pages.wallpaper

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Labels and sizes used by the wallpaper control center page. */
object WallpaperPageDefaults {
    /** Title of the page tab. */
    const val PAGE_NAME = "Wallpapers"

    /** Title of the folders section. */
    const val SECTION_FOLDERS = "Wallpaper folders"

    /** Title of the palette section (colors extracted from the wallpaper). */
    const val SECTION_PALETTE = "Wallpaper palette"

    /** Palette row labels. */
    const val COLOR_BACKGROUND = "Background"
    const val COLOR_TEXT = "Text"
    const val COLOR_ICONS = "Icons"
    const val COLOR_BORDER = "Border"
    const val COLOR_SELECTED = "Selected"
    const val COLOR_SELECTED_TEXT = "Selected text"

    /** Shown when no wallpaper folder exists. */
    const val NO_FOLDERS = "No wallpaper folders found"

    /** Shown in the preview of a folder without images. */
    const val NO_IMAGES = "No images"

    /** Pause between two preview images. */
    const val PREVIEW_DELAY_MS = 1_000L

    /** Preview aspect ratio (16:9). */
    const val PREVIEW_ASPECT_RATIO = 16f / 9f

    /** Corner radius of the preview image. */
    val previewCorner: Dp = 8.dp

    /** Vertical gap between preview and file name. */
    val previewGap: Dp = 4.dp
}

package org.mjdev.desktop.managers.theme.linux.gtk

import java.io.File

/**
 * Stock theme the generated one is layered on, so apps keep the preinstalled Ubuntu look
 * (shapes, spacing, icons) and only the colors follow the desktop palette.
 *
 * Yaru (package `yaru-theme-gtk`, preinstalled on Ubuntu) is preferred; GTK's built-in Adwaita
 * is the fallback when Yaru is missing, so the theme never ends up unstyled.
 *
 * @property lightDir theme directory name of the light variant
 * @property darkDir theme directory name of the dark variant
 */
enum class GtkBaseTheme(
    val lightDir: String,
    val darkDir: String,
) {
    /** Ubuntu's Yaru. */
    YARU("Yaru", "Yaru-dark"),
    ;

    /** Stylesheet file of this theme for [toolkit] / variant under [themesDir], null when not installed. */
    fun cssFile(
        toolkit: GtkToolkit,
        dark: Boolean,
        themesDir: File,
    ): File? = File(themesDir, if (dark) darkDir else lightDir)
        .resolve(toolkit.dirName)
        .resolve(toolkit.cssFileName)
        .takeIf { file -> file.isFile }

    companion object {
        /** System wide theme directory. */
        val SYSTEM_THEMES_DIR = File("/usr/share/themes")

        /** URI scheme for a stylesheet file in `@import`. */
        private const val FILE_URI = "file://"

        /** `@import` target: the first installed stock theme, else GTK's built-in theme. */
        fun importUrl(
            toolkit: GtkToolkit,
            dark: Boolean,
            themesDir: File = SYSTEM_THEMES_DIR,
        ): String = entries
            .firstNotNullOfOrNull { base -> base.cssFile(toolkit, dark, themesDir) }
            ?.let { file -> FILE_URI + file.absolutePath }
            ?: toolkit.builtin(dark)
    }
}

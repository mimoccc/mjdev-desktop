package org.psippr.desktop.managers.theme.linux.gtk

/**
 * GTK major versions a stylesheet is generated for.
 *
 * @property dirName theme / config sub directory (`~/.themes/<name>/<dirName>`, `~/.config/<dirName>`)
 * @property builtinLight GTK's own light theme resource, always available as base
 * @property builtinDark GTK's own dark theme resource, always available as base
 */
enum class GtkToolkit(
    val dirName: String,
    val builtinLight: String,
    val builtinDark: String,
) {
    /** GTK 3 (Firefox, Chromium, Electron, LibreOffice, GIMP 3, most third party apps). */
    GTK3(
        "gtk-3.0",
        "resource:///org/gtk/libgtk/theme/Adwaita/gtk-contained.css",
        "resource:///org/gtk/libgtk/theme/Adwaita/gtk-contained-dark.css",
    ),

    /** GTK 4 apps not using libadwaita (libadwaita apps read `~/.config/gtk-4.0/gtk.css` only). */
    GTK4(
        "gtk-4.0",
        "resource:///org/gtk/libgtk/theme/Default/Default-light.css",
        "resource:///org/gtk/libgtk/theme/Default/Default-dark.css",
    ),
    ;

    /** File name of the stylesheet inside [dirName]. */
    val cssFileName: String
        get() = CSS_FILE_NAME

    /** GTK's built-in theme for the given variant. */
    fun builtin(dark: Boolean): String = if (dark) builtinDark else builtinLight

    companion object {
        /** Stylesheet name GTK loads from a theme / config dir. */
        const val CSS_FILE_NAME = "gtk.css"
    }
}

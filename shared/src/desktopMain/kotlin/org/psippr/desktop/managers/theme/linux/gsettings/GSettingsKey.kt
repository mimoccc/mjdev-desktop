package org.psippr.desktop.managers.theme.linux.gsettings

/**
 * GSettings keys the theme manager switches.
 *
 * @property schema gsettings schema id
 * @property key key inside the schema
 * @property needsExtensionSchema true when the schema ships with a shell extension and is not in
 * the default schema path, so it needs `--schemadir`
 */
enum class GSettingsKey(
    val schema: String,
    val key: String,
    val needsExtensionSchema: Boolean = false,
) {
    /** Light / dark preference followed by libadwaita, GNOME Shell and portals (also flatpak apps). */
    COLOR_SCHEME("org.gnome.desktop.interface", "color-scheme"),

    /** System accent (GNOME 47+), followed live by libadwaita apps and GNOME Shell. */
    ACCENT_COLOR("org.gnome.desktop.interface", "accent-color"),

    /** GTK3 / GTK4 theme name, re-read live by running non-libadwaita GTK apps. */
    GTK_THEME("org.gnome.desktop.interface", "gtk-theme"),

    /** Icon theme name, re-read live by running GTK apps. */
    ICON_THEME("org.gnome.desktop.interface", "icon-theme"),

    /** GNOME Shell stylesheet name of the User Themes extension. */
    SHELL_THEME("org.gnome.shell.extensions.user-theme", "name", true),
}

package eu.mjdev.desktop.helpers.managers

import eu.mjdev.desktop.helpers.system.Command

@Suppress("unused", "MemberVisibilityCanBePrivate")
class GnomeManager {

    fun setColorScheme(
        schemeName: String
    ) = Command(
        "gsettings",
        "set",
        "org.gnome.desktop.interface",
        "color-scheme",
        schemeName
    ).execute()

    fun setGTKTheme(
        themeName: String = THEME_YARU
    ) = Command(
        "gsettings",
        "set",
        "org.gnome.desktop.interface",
        "gtk-theme",
        themeName
    ).execute()

    fun setIconTheme(
        themeName: String = THEME_YARU
    ) = Command(
        "gsettings",
        "set",
        "org.gnome.desktop.interface",
        "icon-theme",
        themeName
    ).execute()

    fun setSoundTheme(
        themeName: String = THEME_YARU
    ) = Command(
        "gsettings",
        "set",
        "org.gnome.desktop.sound",
        "gtk-theme",
        themeName
    ).execute()

    fun setDarkColorScheme() =
        setColorScheme(COLOR_SCHEME_PREFER_DARK)

    companion object {
        const val COLOR_SCHEME_PREFER_DARK = "prefer-dark"

        const val COLOR_SCHEME_MJDEV = "mjdev"

        const val THEME_YARU = "Yaru"
        const val THEME_ADWAITA_DARK = "Adwaita-dark"
        const val THEME_MJDEV = "mjdev"

    }

}
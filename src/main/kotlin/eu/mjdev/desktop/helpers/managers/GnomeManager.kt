package eu.mjdev.desktop.helpers.managers

import eu.mjdev.desktop.helpers.system.Shell

//import org.freedesktop.dbus.annotations.DBusInterfaceName
//import org.freedesktop.dbus.connections.impl.DBusConnection
//import org.freedesktop.dbus.interfaces.DBusInterface
//import org.freedesktop.dbus.types.Variant

@Suppress("unused", "MemberVisibilityCanBePrivate")
class GnomeManager {

//    val connection by lazy { DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION) }
//    val settingsInterface by lazy {  connection.getRemoteObject(
//        "org.freedesktop.portal.Desktop",
//        "/org/freedesktop/portal/desktop",
//        SettingsInterface::class.java
//    ) }
//
//    val currentColorScheme get() = settingsInterface.Read("org.freedesktop.appearance", "color-scheme")

//    init {
//        println(recursiveVariantValue(currentColorScheme))
//    }

    fun setColorScheme(
        schemeName: String
    ) {
        Shell.executeAndRead(
            "gsettings",
            "set",
            "org.gnome.desktop.interface",
            "color-scheme",
            schemeName
        )
    }

    @Suppress("UNUSED_PARAMETER")
    fun setGTKTheme(
        themeName: String = THEME_YARU
    ) {
//        Shell.executeAndRead(
//            "gsettings",
//            "set",
//            "org.gnome.desktop.interface",
//            "gtk-theme",
//            themeName
//        )
    }

    fun getGTKTheme(): String = Shell.executeAndRead(
        "gsettings",
        "get",
        "org.gnome.desktop.interface",
        "gtk-theme"
    ).replace("'", "").trim()

    fun setIconTheme(
        themeName: String = THEME_YARU
    ) = Shell.executeAndRead(
        "gsettings",
        "set",
        "org.gnome.desktop.interface",
        "icon-theme",
        themeName
    )

    fun setSoundTheme(
        themeName: String = THEME_YARU
    ) = Shell.executeAndRead(
        "gsettings",
        "set",
        "org.gnome.desktop.sound",
        "gtk-theme",
        themeName
    )

//    fun setDarkColorScheme() =
//        setColorScheme(COLOR_SCHEME_PREFER_DARK)

//    fun recursiveVariantValue(variant: Variant<*>): Any {
//        val value = variant.value
//        if(value !is Variant<*>) return value
//        else return recursiveVariantValue(value)
//    }

//    @DBusInterfaceName("org.freedesktop.portal.Settings")
//    interface SettingsInterface : DBusInterface {
//        fun Read(namespace: String, key: String): Variant<*>
//    }

    companion object {
        const val COLOR_SCHEME_PREFER_DARK = "prefer-dark"

        const val COLOR_SCHEME_MJDEV = "mjdev"

        const val THEME_YARU = "Yaru"
        const val THEME_ADWAITA = "Adwaita"
        const val THEME_ADWAITA_DARK = "Adwaita-dark"
        const val THEME_MJDEV = "Mjdev"
    }

}
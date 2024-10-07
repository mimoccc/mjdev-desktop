package eu.mjdev.desktop.helpers.system

import eu.mjdev.desktop.managers.GnomeManager.Companion.THEME_MJDEV

class Environment(
    val data: List<String> = Shell.executeAndReadLines("env")
) : HashMap<String, String>() {
    init {
        putAll(System.getenv())
        put(DBUS_SESSION_BUS_ADDRESS, DBUS_SESSION_BUS_ADDRESS_DEFAULT)
        put(XDG_CURRENT_DESKTOP, DESKTOP_UBUNTU_GNOME)
        put(GTK_THEME, THEME_MJDEV)
    }

    companion object {
        const val DBUS_SESSION_BUS_ADDRESS = "DBUS_SESSION_BUS_ADDRESS"
        const val XDG_CURRENT_DESKTOP = "XDG_CURRENT_DESKTOP"
        const val GTK_THEME = "GTK_THEME"

        const val DBUS_SESSION_BUS_ADDRESS_DEFAULT = "unix:path=\$XDG_RUNTIME_DIR/bus"
        const val DESKTOP_UBUNTU_GNOME = "ubuntu:GNOME"
    }
}

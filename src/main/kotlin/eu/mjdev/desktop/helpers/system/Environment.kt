package eu.mjdev.desktop.helpers.system

import eu.mjdev.desktop.helpers.system.Shell.Companion.CMD_ENV
import eu.mjdev.desktop.managers.theme.linux.ThemeManagerLinux.Companion.THEME_MJDEV

@Suppress("unused")
class Environment(
    val data: List<String> = Shell.executeAndReadLines(CMD_ENV)
) : HashMap<String, String>() {
    init {
        putAll(System.getenv())
        put(DBUS_SESSION_BUS_ADDRESS, DBUS_SESSION_BUS_ADDRESS_DEFAULT)
        put(XDG_CURRENT_DESKTOP, DESKTOP_UBUNTU_GNOME)
        put(GTK_THEME, THEME_MJDEV)
    }

    fun toTypedArray() = map { p ->
        "${p.key}=${p.value}"
    }.toTypedArray()

    companion object {
        const val DBUS_SESSION_BUS_ADDRESS = "DBUS_SESSION_BUS_ADDRESS"
        const val XDG_CURRENT_DESKTOP = "XDG_CURRENT_DESKTOP"
        const val GTK_THEME = "GTK_THEME"

        const val DBUS_SESSION_BUS_ADDRESS_DEFAULT = "unix:path=\$XDG_RUNTIME_DIR/bus"
        const val DESKTOP_UBUNTU_GNOME = "ubuntu:GNOME"
    }
}

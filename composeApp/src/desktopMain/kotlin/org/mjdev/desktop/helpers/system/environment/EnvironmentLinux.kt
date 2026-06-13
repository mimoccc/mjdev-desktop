package org.mjdev.desktop.helpers.system.environment

import org.mjdev.desktop.managers.theme.linux.ThemeManagerLinux.Companion.THEME_MJDEV

class EnvironmentLinux :
    EnvironmentStub(
        getenv = { System.getenv() },
    ) {
    init {
        data[DBUS_SESSION_BUS_ADDRESS] = DBUS_SESSION_BUS_ADDRESS_DEFAULT
        data[XDG_CURRENT_DESKTOP] = DESKTOP_UBUNTU_GNOME
        data[GTK_THEME] = THEME_MJDEV
    }

    companion object {
        const val DBUS_SESSION_BUS_ADDRESS = "DBUS_SESSION_BUS_ADDRESS"
        const val XDG_CURRENT_DESKTOP = "XDG_CURRENT_DESKTOP"
        const val GTK_THEME = "GTK_THEME"

        const val DBUS_SESSION_BUS_ADDRESS_DEFAULT = "unix:path=\$XDG_RUNTIME_DIR/bus"
        const val DESKTOP_UBUNTU_GNOME = "ubuntu:GNOME"
    }
}

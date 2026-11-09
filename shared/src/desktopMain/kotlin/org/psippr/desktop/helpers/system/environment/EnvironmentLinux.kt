package org.psippr.desktop.helpers.system.environment

import org.psippr.desktop.managers.theme.linux.ThemeManagerLinux.Companion.THEME_PSIPPR

/** Environment of processes started on Linux: the session's own, plus the desktop's overrides. */
class EnvironmentLinux(
    getenv: () -> Map<String, String> = { System.getenv() },
) : EnvironmentStub(
        getenv = getenv,
    ) {
    init {
        // old: the literal "$XDG_RUNTIME_DIR" was never expanded, so every launched app got a bus
        // path that does not exist (gnome-terminal: "Could not connect: No such file or directory")
        // data[DBUS_SESSION_BUS_ADDRESS] = DBUS_SESSION_BUS_ADDRESS_DEFAULT
        sessionBusAddress()?.let { address -> data[DBUS_SESSION_BUS_ADDRESS] = address }
        data[XDG_CURRENT_DESKTOP] = DESKTOP_UBUNTU_GNOME
        data[GTK_THEME] = THEME_PSIPPR
        // the x11 backend Java's AWT put into this process is not passed on to started apps
        InheritedGdkBackend.restore(data)
    }

    /** The session's bus address when set, else the user bus socket in the runtime dir. */
    private fun sessionBusAddress(): String? = data[DBUS_SESSION_BUS_ADDRESS]
        ?: data[XDG_RUNTIME_DIR]?.let { dir -> DBUS_ADDRESS_PREFIX + dir + DBUS_SOCKET }

    companion object {
        /** Variable holding the D-Bus session bus address. */
        const val DBUS_SESSION_BUS_ADDRESS = "DBUS_SESSION_BUS_ADDRESS"

        /** Variable naming the running desktop. */
        const val XDG_CURRENT_DESKTOP = "XDG_CURRENT_DESKTOP"

        /** Variable forcing the GTK theme. */
        const val GTK_THEME = "GTK_THEME"

        /** Variable holding the user's runtime dir (`/run/user/<uid>`). */
        const val XDG_RUNTIME_DIR = "XDG_RUNTIME_DIR"

        /** Old: unexpanded fallback bus address, no longer used (see init). */
        const val DBUS_SESSION_BUS_ADDRESS_DEFAULT = "unix:path=\$XDG_RUNTIME_DIR/bus"

        /** Address prefix of a unix socket bus. */
        const val DBUS_ADDRESS_PREFIX = "unix:path="

        /** User bus socket inside the runtime dir. */
        const val DBUS_SOCKET = "/bus"

        /** Desktop name given to launched apps. */
        const val DESKTOP_UBUNTU_GNOME = "ubuntu:GNOME"
    }
}

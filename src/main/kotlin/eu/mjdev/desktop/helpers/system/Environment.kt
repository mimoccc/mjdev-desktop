package eu.mjdev.desktop.helpers.system

import eu.mjdev.desktop.helpers.system.Shell.Companion.CMD_ENV
import java.io.File

@Suppress("unused")
class Environment(
    val data: List<String> = Shell.executeAndReadLines(CMD_ENV)
) : HashMap<String, String>() {
    init {
        putAll(System.getenv())
        put(DBUS_SESSION_BUS_ADDRESS, DBUS_SESSION_BUS_ADDRESS_DEFAULT)
        if (isMjdevSession) {
            put(XDG_CURRENT_DESKTOP, DESKTOP_MJDEV)
        } else if (!containsKey(XDG_CURRENT_DESKTOP)) {
            put(XDG_CURRENT_DESKTOP, DESKTOP_UBUNTU_GNOME)
        }
        // GTK_THEME must not be forced here: a fixed GTK_THEME env var
        // freezes the theme and blocks live theme switching in gtk apps
        remove(GTK_THEME)
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
        const val DESKTOP_MJDEV = "mjdev"

        /** true when the desktop runs inside the mjdev compositor session */
        val isMjdevSession: Boolean
            get() = System.getenv(XDG_CURRENT_DESKTOP) == DESKTOP_MJDEV ||
                    System.getenv("XDG_RUNTIME_DIR")
                        ?.let { File("$it/mjdev-compositor.sock").exists() } == true
    }
}

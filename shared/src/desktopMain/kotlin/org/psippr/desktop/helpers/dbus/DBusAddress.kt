package org.psippr.desktop.helpers.dbus

import java.nio.file.Path

/** Reads a D-Bus address such as `unix:path=/run/user/1000/bus,guid=...` */
object DBusAddress {
    /** Environment variable holding the session bus address. */
    const val SESSION_ENV = "DBUS_SESSION_BUS_ADDRESS"

    private const val UNIX_PATH = "unix:path="
    private const val ENTRY_SEPARATOR = ";"
    private const val KEY_SEPARATOR = ","

    /**
     * Socket file of the first `unix:path=` entry of [address], null when there is none (abstract
     * sockets cannot be opened from Java, and other transports are not used here).
     */
    fun socketPath(address: String?): Path? = address
        ?.split(ENTRY_SEPARATOR)
        ?.firstOrNull { entry -> entry.startsWith(UNIX_PATH) }
        ?.removePrefix(UNIX_PATH)
        ?.substringBefore(KEY_SEPARATOR)
        ?.let { path -> Path.of(path) }
}

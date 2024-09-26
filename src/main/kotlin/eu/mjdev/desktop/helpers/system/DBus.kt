package eu.mjdev.desktop.helpers.system

class DBus {
    fun updateEnvironment(): String =
        Shell.executeAndRead("dbus-update-activation-environment")

    // todo
    fun send(vararg args: String): String =
        Shell.executeAndRead("dbus-send", *args)
}
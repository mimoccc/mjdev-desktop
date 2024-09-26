package eu.mjdev.desktop.helpers.system

// todo from bash
class Environment(
    val data: String = Command("env").execute() ?: ""
) : ArrayList<String>() {
    init {
        addAll(data.split("\n"))
        add("export DBUS_SESSION_BUS_ADDRESS=\"unix:path=\$XDG_RUNTIME_DIR/bus\"")
    }
}
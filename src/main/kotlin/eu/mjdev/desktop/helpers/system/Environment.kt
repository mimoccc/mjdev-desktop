package eu.mjdev.desktop.helpers.system

class Environment(
    val data : List<String> = Shell.executeAndReadLines("env")
) : ArrayList<String>() {
    init {
        addAll(data)
        add("export DBUS_SESSION_BUS_ADDRESS=\"unix:path=\$XDG_RUNTIME_DIR/bus\"")
    }
}
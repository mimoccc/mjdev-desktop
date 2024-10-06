package eu.mjdev.desktop.helpers.system

class Environment(
    val data: List<String> = Shell.executeAndReadLines("env")
) : HashMap<String, String>() {
    init {
        putAll(System.getenv())
        put("DBUS_SESSION_BUS_ADDRESS", "unix:path=\$XDG_RUNTIME_DIR/bus")
        put("XDG_CURRENT_DESKTOP", "ubuntu:GNOME")
    }
}
package eu.mjdev.desktop.helpers.system

// todo from bash
class Environment(
    val data: String = Command("env").execute() ?: ""
) : ArrayList<String>() {
    init {
        addAll(data.split("\n"))
    }
}
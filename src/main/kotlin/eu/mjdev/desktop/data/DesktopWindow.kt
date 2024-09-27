package eu.mjdev.desktop.data

data class DesktopWindow(
    val id: Long,
    val pid: Long,
    val name: String,
    val command: String,
    val iconName: String,
)

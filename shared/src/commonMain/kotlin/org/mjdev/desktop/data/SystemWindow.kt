package org.mjdev.desktop.data

data class SystemWindow(
    val id: Long,
    val pid: Long,
    val name: String,
    val command: String,
    val iconName: String,
)

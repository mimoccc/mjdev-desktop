package org.mjdev.desktop.interfaces

import org.mjdev.desktop.data.Category

interface IApp {
    val name: String
    val fullAppName: String
    val comment: String
    val cmd: String
    val isStarting: Boolean
    val isRunning: Boolean
    val fullTextString: String
    val categories: List<Category>

    suspend fun start()
}
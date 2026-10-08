package org.psippr.desktop.interfaces

import org.psippr.desktop.data.Category

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

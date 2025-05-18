package org.mjdev.desktop.interfaces

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface IPage : IDisposable {
    val name: String
    val icon: ImageVector
    val condition: IDesktopContext.() -> Boolean
    val saver: IControlCenterPageDataSaver?

    @Composable
    fun Render()
}
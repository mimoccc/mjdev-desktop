package org.psippr.desktop.interfaces

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.psippr.desktop.components.controlcenter.base.ControlCenterPageScope
import org.psippr.desktop.components.controlcenter.base.PageCache
import org.psippr.desktop.context.IDesktopContext

interface IPage : IDisposable {
    val context: IDesktopContext
    val name: String
    val icon: ImageVector
    val condition: IDesktopContext.() -> Boolean
    val saver: (context: IDesktopContext) -> IControlCenterPageDataSaver?
    val cache: PageCache
    val showHeader: Boolean
    val content: @Composable ControlCenterPageScope.() -> Unit

    @Composable
    fun Render()
}

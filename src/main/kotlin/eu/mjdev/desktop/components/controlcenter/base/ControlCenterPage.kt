package eu.mjdev.desktop.components.controlcenter.base

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import eu.mjdev.desktop.components.controlcenter.base.ControlCenterPageScope.Companion.withControlCenterPageScope
import eu.mjdev.desktop.provider.DesktopProvider

@Suppress("MemberVisibilityCanBePrivate")
class ControlCenterPage(
    val icon: ImageVector = Icons.Filled.Settings,
    val name: String = "",
    val condition: DesktopProvider.() -> Boolean = { true },
    val saver: IControlCenterPageDataSaver? = null,
    val cache: PageCache = PageCache(saver),
    val content: @Composable ControlCenterPageScope.() -> Unit = {},
) {
    @Composable
    fun render() = withControlCenterPageScope(cache = cache) {
        content()
    }
}
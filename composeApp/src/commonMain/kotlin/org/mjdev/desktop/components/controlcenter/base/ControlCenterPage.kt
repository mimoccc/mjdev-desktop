package org.mjdev.desktop.components.controlcenter.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPageScope.Companion.withControlCenterPageScope
import org.mjdev.desktop.interfaces.IControlCenterPageDataSaver
import org.mjdev.desktop.interfaces.IDesktopContext
import org.mjdev.desktop.interfaces.IPage
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("MemberVisibilityCanBePrivate")
class ControlCenterPage(
    override val icon: ImageVector = Icons.Filled.Settings,
    override val name: String = "",
    override val condition: IDesktopContext.() -> Boolean = { true },
    override val saver: IControlCenterPageDataSaver? = null,
    val cache: PageCache = PageCache(saver),
    val showHeader: Boolean = true,
//    val scrollState: ScrollState = ScrollState(0),
    val content: @Composable ControlCenterPageScope.() -> Unit = {},
) : IPage {
    @Composable
    override fun render() {
        withControlCenterPageScope(
            cache = cache
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                PageHeader(
                    page = this@ControlCenterPage
                )
                Box(
                    modifier = Modifier.weight(1f),
                ) {
                    content()
                }
//            VerticalScrollbar(
//                modifier = Modifier.fillMaxHeight(),
//                adapter = rememberScrollbarAdapter(scrollState)
//            )
            }
        }
    }

    override fun dispose() {
        // todo
    }
}

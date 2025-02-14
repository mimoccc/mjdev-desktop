package org.mjdev.desktop.components.controlcenter.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import org.mjdev.desktop.context.DesktopContextScope
import org.mjdev.desktop.context.LocalDesktopContext
import org.mjdev.desktop.interfaces.IDesktopContext

class ControlCenterPageScope(
    context: IDesktopContext,
    val cache: PageCache
) : DesktopContextScope(context) {
    companion object {
        @Suppress("UnusedReceiverParameter")
        @Composable
        fun ControlCenterPage.withControlCenterPageScope(
            context: IDesktopContext = LocalDesktopContext.current,
            cache: PageCache,
            block: @Composable ControlCenterPageScope.() -> Unit
        ) = ControlCenterPageScope(context, cache).apply {
            block()
        }

        @Composable
        fun <T> ControlCenterPageScope.remember(
            calculation: @DisallowComposableCalls () -> T
        ): T = cache.cache(false, calculation)
    }
}

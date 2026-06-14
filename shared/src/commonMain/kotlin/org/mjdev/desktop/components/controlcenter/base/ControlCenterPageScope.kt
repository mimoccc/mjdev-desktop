package org.mjdev.desktop.components.controlcenter.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import org.mjdev.desktop.context.DesktopContextScope
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.context.LocalDesktopContext

@Suppress("UnusedReceiverParameter", "ComposableNaming")
class ControlCenterPageScope(
    context: IDesktopContext,
    private val cache: PageCache,
) : DesktopContextScope(context) {
    companion object {
        @Composable
        fun ControlCenterPage.withControlCenterPageScope(
            cache: PageCache,
            context: IDesktopContext = LocalDesktopContext.current,
            block: @Composable ControlCenterPageScope.() -> Unit,
        ) {
            block(ControlCenterPageScope(context, cache))
        }

        @Composable
        fun <T> ControlCenterPageScope.remember(calculation: @DisallowComposableCalls () -> T): T = cache.cache(false, calculation)

        @Composable
        fun <T> ControlCenterPageScope.rememberComputed(
            vararg key: Any?,
            calculation: () -> T,
        ) = remember(*key) { derivedStateOf { calculation() } }
    }
}

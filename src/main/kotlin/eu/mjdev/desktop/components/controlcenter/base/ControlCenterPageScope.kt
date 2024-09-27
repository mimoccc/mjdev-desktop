package eu.mjdev.desktop.components.controlcenter.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.DesktopScope

class ControlCenterPageScope(
    api: DesktopProvider,
    val cache: PageCache
) : DesktopScope(api) {
    companion object {
        @Suppress("UnusedReceiverParameter")
        @Composable
        fun ControlCenterPage.withControlCenterPageScope(
            api: DesktopProvider = LocalDesktop.current,
            cache: PageCache,
            block: @Composable ControlCenterPageScope.() -> Unit
        ) = ControlCenterPageScope(api, cache).apply {
            block()
        }

        @Composable
        fun <T> ControlCenterPageScope.remember(
            calculation: @DisallowComposableCalls () -> T
        ): T = cache.cache(false, calculation)
    }
}

package eu.mjdev.desktop.components.controlcenter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage.ControlCenterPageScope.PageCache
import eu.mjdev.desktop.provider.DesktopProvider

@Suppress("UNCHECKED_CAST")
class ControlCenterPage(
    val icon: ImageVector = Icons.Filled.Settings,
    val name: String = "",
    val condition: DesktopProvider.() -> Boolean = { true },
    private val saver: IControlCenterPageDataSaver? = null,
    val cache: PageCache = PageCache(saver),
    val content: @Composable ControlCenterPageScope.() -> Unit = {},
) {
    class ControlCenterPageScope(
        val api: DesktopProvider,
        private val backgroundColorInvoker: () -> Color,
        private val cache: PageCache
    ) {
        val scope
            get() = api.scope

        val backgroundColor
            get() = backgroundColorInvoker()

        @Composable
        fun <T> remember(
            calculation: @DisallowComposableCalls () -> T
        ): T = cache.cache(false, calculation)

        @Suppress("unused")
        class PageCache(
            private val saver: IControlCenterPageDataSaver? = null
        ) : HashMap<Int, Any>() {
            init {
                saver?.load()?.onEach { (t, u) -> this[t] = u }
            }

            fun save() {
                saver?.save(this)
            }

            fun <T> cache(
                invalid: Boolean,
                block: @DisallowComposableCalls () -> T
            ): T {
                val hash = block.hashCode()
                var data: T = this[hash] as T
                if (invalid || data == null) {
                    data = block()
                }
                if (data != null) this[hash] = data
                return data
            }
        }
    }
}
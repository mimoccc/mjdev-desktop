package org.mjdev.desktop.components.desktop.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext

/** Desktop widget layer: shows every plugin the user enabled in the control center. */
@Suppress("FunctionName")
@Composable
fun PluginWidgets() = withDesktopContext {
    pluginManager.plugins
        .filter { info -> info.enabled }
        .forEach { info ->
            key(info.id) {
                val plugin = remember(info.id) { pluginManager.instance(info.id) }
                if (plugin != null) PluginWidget(plugin)
            }
        }
}

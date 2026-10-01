package org.mjdev.desktop.components.desktop.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.mjdev.desktop.components.draggable.DraggableView
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.plugins.IDesktopPlugin
import org.mjdev.desktop.plugins.remote.RemoteAlign
import org.mjdev.desktop.plugins.remote.RemoteDocumentPlayer
import org.mjdev.desktop.plugins.remote.RemoteVariables

/**
 * Draws one enabled [plugin] on the desktop: its document is rendered by [RemoteDocumentPlayer]
 * and its variables are refreshed off the UI thread every [IDesktopPlugin.refreshMs]. The widget
 * can be dragged like the other desktop widgets.
 */
@Suppress("FunctionName")
@Composable
fun PluginWidget(
    plugin: IDesktopPlugin,
    modifier: Modifier = Modifier,
) = withDesktopContext {
    val document = remember(plugin) { plugin.document }
    var variables by remember(plugin) { mutableStateOf(RemoteVariables()) }
    LaunchedEffect(plugin) {
        while (isActive) {
            // A failing plugin keeps its last values instead of crashing the desktop.
            variables =
                withContext(Dispatchers.Default) {
                    runCatching { plugin.variables(context) }
                        .onFailure { e -> Log.e(e) }
                        .getOrDefault(variables)
                }
            delay(plugin.refreshMs)
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        DraggableView(
            modifier =
                Modifier
                    .size(document.width.dp, document.height.dp)
                    .align(RemoteAlign.parse(document.anchor, RemoteAlign.BOTTOM_END).alignment),
        ) {
            RemoteDocumentPlayer(
                document = document,
                variables = variables,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

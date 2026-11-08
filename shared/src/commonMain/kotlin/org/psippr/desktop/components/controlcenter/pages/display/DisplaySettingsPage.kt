package org.psippr.desktop.components.controlcenter.pages.display

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.psippr.desktop.components.controlcenter.base.ControlCenterPage
import org.psippr.desktop.components.controlcenter.settings.SettingsInfoRow
import org.psippr.desktop.components.controlcenter.settings.SettingsPageColumn
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.components.controlcenter.settings.SettingsSelectRow
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.icons.settings.SettingsMonitor
import org.psippr.desktop.managers.display.DisplayMode

/** Static labels used by [DisplaySettingsPage]. */
private object DisplayLabels {
    const val SCREEN = "Screen"
    const val WIDTH = "Width"
    const val HEIGHT = "Height"
    const val LAYOUT = "Layout"
    const val PANEL = "Panel location"
    const val CONTROL_CENTER = "Control center"
    const val UNIT = "dp"
    const val RESOLUTION = "Resolution"
    const val UNKNOWN = "-"
    const val REFUSED = "The screen did not accept this resolution."
}

/**
 * Control center page of the screen: its resolution (switchable under the psippr compositor and
 * GNOME, see [org.psippr.desktop.managers.display.IDisplayManager]), the desktop surface size and
 * where the panels are placed.
 */
@Suppress("FunctionName")
fun DisplaySettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = Icons.Filled.Monitor,
    name = "Display",
) {
    val manager = context.displayManager
    val scope = rememberCoroutineScope()
    var current by remember { mutableStateOf<DisplayMode?>(null) }
    var modes by remember { mutableStateOf<List<DisplayMode>>(emptyList()) }
    var refused by remember { mutableStateOf(false) }

    suspend fun reload() = withContext(Dispatchers.Default) {
        current = manager.currentMode
        modes = manager.modes
    }
    LaunchedEffect(Unit) { reload() }
    // old: size only, the resolution could not be seen or changed
//    SettingsPageColumn {
//        SettingsSection(title = DisplayLabels.SCREEN) {
//            SettingsInfoRow(DisplayLabels.WIDTH, "${containerSize.width.value.toInt()} ${DisplayLabels.UNIT}")
//            SettingsInfoRow(DisplayLabels.HEIGHT, "${containerSize.height.value.toInt()} ${DisplayLabels.UNIT}")
//        }
    SettingsPageColumn {
        SettingsSection(title = DisplayLabels.SCREEN) {
            val selected = current
            if (modes.isNotEmpty() && selected != null) {
                SettingsSelectRow(
                    label = DisplayLabels.RESOLUTION,
                    selected = selected.label,
                    options = modes.map { mode -> mode.label },
                ) { label ->
                    val mode = modes.firstOrNull { it.label == label } ?: return@SettingsSelectRow
                    scope.launch {
                        refused = !withContext(Dispatchers.Default) { manager.setMode(mode) }.getOrDefault(false)
                        reload()
                    }
                }
            } else {
                SettingsInfoRow(DisplayLabels.RESOLUTION, selected?.label ?: DisplayLabels.UNKNOWN)
            }
            if (refused) {
                SettingsInfoRow(DisplayLabels.REFUSED, "")
            }
            SettingsInfoRow(DisplayLabels.WIDTH, "${containerSize.width.value.toInt()} ${DisplayLabels.UNIT}")
            SettingsInfoRow(DisplayLabels.HEIGHT, "${containerSize.height.value.toInt()} ${DisplayLabels.UNIT}")
        }
        SettingsSection(title = DisplayLabels.LAYOUT) {
            SettingsInfoRow(DisplayLabels.PANEL, panelLocation.name)
            SettingsInfoRow(DisplayLabels.CONTROL_CENTER, theme.controlCenterLocation.name)
        }
    }
}

@Preview
@Composable
fun DisplaySettingsPagePreview() = preview {
    DisplaySettingsPage(context).Render()
}

package org.mjdev.desktop.components.controlcenter.pages.display

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.components.controlcenter.settings.SettingsPageColumn
import org.mjdev.desktop.components.controlcenter.settings.SettingsSection
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.components.controlcenter.settings.SettingsInfoRow
import org.mjdev.desktop.icons.settings.SettingsMonitor

/** Static labels used by [DisplaySettingsPage]. */
private object DisplayLabels {
    const val SCREEN = "Screen"
    const val WIDTH = "Width"
    const val HEIGHT = "Height"
    const val LAYOUT = "Layout"
    const val PANEL = "Panel location"
    const val CONTROL_CENTER = "Control center"
    const val UNIT = "dp"
}

/** Control center page describing the desktop surface size and where the panels are placed. */
@Suppress("FunctionName")
fun DisplaySettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = SettingsMonitor,
    name = "Display",
) {
    SettingsPageColumn {
        SettingsSection(title = DisplayLabels.SCREEN) {
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

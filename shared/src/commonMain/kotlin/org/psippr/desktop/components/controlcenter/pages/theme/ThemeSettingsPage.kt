package org.psippr.desktop.components.controlcenter.pages.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import org.psippr.desktop.components.controlcenter.base.ControlCenterPage
import org.psippr.desktop.components.controlcenter.settings.SettingsColorRow
import org.psippr.desktop.components.controlcenter.settings.SettingsPageColumn
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.icons.custom.Psippr

/** Static labels used by [ThemeSettingsPage]. */
private object ThemeLabels {
    const val PALETTE = "Wallpaper palette"
    const val BACKGROUND = "Background"
    const val TEXT = "Text"
    const val ICONS = "Icons"
    const val BORDER = "Border"
    const val SELECTED = "Selected"
    const val SELECTED_TEXT = "Selected text"
}

/**
 * Control center page showing the colors the whole desktop is painted with. They are not
 * configured here - the [org.psippr.desktop.managers.palette.IPalette] extracts them from the
 * current wallpaper, so every control center page follows the background image.
 */
@Suppress("FunctionName")
fun ThemeSettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = Icons.Filled.Palette,
    name = "Theme",
    condition = { true },
) {
    SettingsPageColumn {
        SettingsSection(title = ThemeLabels.PALETTE) {
            SettingsColorRow(ThemeLabels.BACKGROUND, backgroundColor)
            SettingsColorRow(ThemeLabels.TEXT, textColor)
            SettingsColorRow(ThemeLabels.ICONS, iconsTintColor)
            SettingsColorRow(ThemeLabels.BORDER, borderColor)
            SettingsColorRow(ThemeLabels.SELECTED, selectedBgColor)
            SettingsColorRow(ThemeLabels.SELECTED_TEXT, selectedFgColor)
        }
    }
}

@Preview
@Composable
fun ThemeSettingsPagePreview() = preview {
    ThemeSettingsPage(context).Render()
}

package org.mjdev.desktop.components.controlcenter.pages.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.components.controlcenter.settings.SettingsInfoRow
import org.mjdev.desktop.components.controlcenter.settings.SettingsPageColumn
import org.mjdev.desktop.components.controlcenter.settings.SettingsSection
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview

/**
 * Control center page listing every plugin found in `~/.mjdev/plugins/` (plus the built-in
 * ones). Each row shows the plugin image and label with a switch that shows or hides its widget
 * on the desktop; clicking the row expands its details.
 */
@Suppress("FunctionName")
fun PluginsSettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = PluginsPageDefaults.fallbackIcon,
    name = "Plugins",
    condition = { true },
) {
    val manager = context.pluginManager
    // Pick up jars that were dropped into the folder while the desktop was running.
    LaunchedEffect(Unit) { manager.refresh() }
    SettingsPageColumn {
        SettingsSection(title = PluginsPageDefaults.SECTION_TITLE) {
            manager.plugins.forEach { info ->
                PluginRow(
                    info = info,
                    onEnabledChange = { enabled -> manager.setEnabled(info.id, enabled) },
                )
            }
        }
        SettingsSection(title = PluginsPageDefaults.FOLDER_TITLE) {
            SettingsInfoRow(label = PluginsPageDefaults.LABEL_PATH, value = manager.pluginsDir)
            TextAny(
                text = PluginsPageDefaults.EMPTY_HINT,
                color = textColor.alpha(PluginsPageDefaults.SECONDARY_ALPHA),
                fontSize = 12.sp,
            )
        }
    }
}

@Preview
@Composable
fun PluginsSettingsPagePreview() = preview {
    PluginsSettingsPage(context).Render()
}

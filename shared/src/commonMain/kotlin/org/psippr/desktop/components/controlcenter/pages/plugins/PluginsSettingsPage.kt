package org.psippr.desktop.components.controlcenter.pages.plugins

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.psippr.desktop.components.controlcenter.base.ControlCenterPage
import org.psippr.desktop.components.controlcenter.settings.SettingsButtonRow
import org.psippr.desktop.components.controlcenter.settings.SettingsInfoRow
import org.psippr.desktop.components.controlcenter.settings.SettingsPageColumn
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.components.text.TextAny
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.managers.plugins.PluginInstallResult

/**
 * Control center page listing every plugin found in `~/.psippr-desktop/plugins/` (plus the built-in
 * ones). Each row shows the plugin image and label with a switch that shows or hides its widget
 * on the desktop; clicking the row expands its details.
 */
@Suppress("FunctionName")
fun PluginsSettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = Icons.Filled.Extension,
    name = "Plugins",
    condition = { true },
) {
    val manager = context.pluginManager
    val scope = rememberCoroutineScope()
    var installing by remember { mutableStateOf(false) }
    var lastInstall by remember { mutableStateOf<PluginInstallResult?>(null) }
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
        // old: showed the folder path and asked to copy jars there by hand
//        SettingsSection(title = PluginsPageDefaults.FOLDER_TITLE) {
//            SettingsInfoRow(label = PluginsPageDefaults.LABEL_PATH, value = manager.pluginsDir)
//            TextAny(
//                text = PluginsPageDefaults.EMPTY_HINT,
//                color = textColor.alpha(PluginsPageDefaults.SECONDARY_ALPHA),
//                fontSize = 12.sp,
//            )
//        }
        SettingsSection(title = PluginsPageDefaults.INSTALL_TITLE) {
            SettingsButtonRow(
                label = PluginsPageDefaults.INSTALL_LABEL,
                action = if (installing) PluginsPageDefaults.INSTALL_BUSY else PluginsPageDefaults.INSTALL_ACTION,
                enabled = !installing,
            ) {
                scope.launch {
                    installing = true
                    // the file dialog blocks until it closes, so it runs off the UI thread
                    lastInstall =
                        withContext(Dispatchers.IO) {
                            manager.chooseArchive()?.let { archive -> manager.install(archive) }
                        } ?: lastInstall
                    installing = false
                }
            }
            lastInstall?.let { result ->
                if (result.succeeded) {
                    SettingsInfoRow(
                        label = PluginsPageDefaults.INSTALLED_PREFIX + result.installed.joinToString(PluginsPageDefaults.LIST_SEPARATOR),
                        value = "",
                    )
                }
                result.problems.forEach { problem -> SettingsInfoRow(label = problem, value = "") }
            }
            TextAny(
                text = PluginsPageDefaults.INSTALL_HINT,
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

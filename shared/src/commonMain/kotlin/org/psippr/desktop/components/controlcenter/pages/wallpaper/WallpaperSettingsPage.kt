package org.psippr.desktop.components.controlcenter.pages.wallpaper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.psippr.desktop.components.controlcenter.base.ControlCenterPage
import org.psippr.desktop.components.controlcenter.settings.SettingsColorRow
import org.psippr.desktop.components.controlcenter.settings.SettingsPageColumn
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.components.text.TextAny
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.data.DesktopConfigStore
import org.psippr.desktop.icons.settings.SettingsMonitor
import org.psippr.desktop.providers.background.WallpaperFolderScanner

/**
 * Control center page for everything wallpaper related: the palette extracted from the current
 * wallpaper (moved here from the theme page) and the wallpaper sub-folders found in the standard background locations.
 * Each folder can be included/excluded with a switch; the choice is persisted in the desktop
 * config and the running desktop re-reads its backgrounds immediately.
 */
@Suppress("FunctionName")
fun WallpaperSettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = Icons.Filled.Wallpaper,
    name = WallpaperPageDefaults.PAGE_NAME,
) {
    val store = remember { DesktopConfigStore(currentUser) }
    val folders = remember { WallpaperFolderScanner.folders(currentUser.userDirs) }
    var excluded by remember { mutableStateOf(store.load().excludedWallpaperFolders.toSet()) }

    SettingsPageColumn {
        SettingsSection(title = WallpaperPageDefaults.SECTION_PALETTE) {
            SettingsColorRow(WallpaperPageDefaults.COLOR_BACKGROUND, backgroundColor)
            SettingsColorRow(WallpaperPageDefaults.COLOR_TEXT, textColor)
            SettingsColorRow(WallpaperPageDefaults.COLOR_ICONS, iconsTintColor)
            SettingsColorRow(WallpaperPageDefaults.COLOR_BORDER, borderColor)
            SettingsColorRow(WallpaperPageDefaults.COLOR_SELECTED, selectedBgColor)
            SettingsColorRow(WallpaperPageDefaults.COLOR_SELECTED_TEXT, selectedFgColor)
        }
        SettingsSection(title = WallpaperPageDefaults.SECTION_FOLDERS) {
            if (folders.isEmpty()) {
                TextAny(
                    text = WallpaperPageDefaults.NO_FOLDERS,
                    color = textColor,
                )
            }
            folders.forEach { folder ->
                WallpaperFolderRow(
                    folder = folder,
                    included = folder.key !in excluded,
                    onIncludedChange = { include ->
                        excluded = if (include) excluded - folder.key else excluded + folder.key
                        val data = store.load().also { it.excludedWallpaperFolders = excluded.toMutableList() }
                        store.save(data)
                        runAsync {
                            currentUser.config.reloadBackgrounds(data.buildProviders(currentUser))
                        }
                    },
                )
            }
        }
    }
}

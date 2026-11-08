package org.psippr.desktop.components.controlcenter.pages.wallpaper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.components.controlcenter.settings.SettingsSelectRow
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.data.DesktopConfigData
import org.psippr.desktop.data.DesktopConfigStore
import org.psippr.desktop.managers.palette.ThemeAccent
import org.psippr.desktop.managers.palette.ThemeMode

/**
 * Style choices on top of the wallpaper colors: the [ThemeMode] (light / dark) and the
 * [ThemeAccent]. A choice is saved with the desktop config and the palette recomputes at once, so
 * the desktop, the apps (GTK theme) and every later wallpaper follow it.
 */
@Suppress("FunctionName")
@Composable
fun WallpaperStyleSection() = withDesktopContext {
    val store = remember { DesktopConfigStore(currentUser) }
    var data by remember { mutableStateOf(store.load()) }

    // reloads first so a value another tab saved meanwhile is kept, then lets the palette recompute
    fun choose(change: (DesktopConfigData) -> DesktopConfigData) {
        data = change(store.load())
        store.save(data)
        context.palette.refresh()
    }
    SettingsSection(title = WallpaperPageDefaults.SECTION_STYLE) {
        SettingsSelectRow(
            label = WallpaperPageDefaults.STYLE_MODE,
            selected = ThemeMode.of(data.themeMode).label,
            options = ThemeMode.entries.map { mode -> mode.label },
        ) { label ->
            ThemeMode.entries
                .firstOrNull { mode -> mode.label == label }
                ?.let { mode -> choose { config -> config.copy(themeMode = mode.name) } }
        }
        SettingsSelectRow(
            label = WallpaperPageDefaults.STYLE_ACCENT,
            selected = ThemeAccent.of(data.themeAccent).label,
            options = ThemeAccent.entries.map { accent -> accent.label },
        ) { label ->
            ThemeAccent.entries
                .firstOrNull { accent -> accent.label == label }
                ?.let { accent -> choose { config -> config.copy(themeAccent = accent.name) } }
        }
    }
}

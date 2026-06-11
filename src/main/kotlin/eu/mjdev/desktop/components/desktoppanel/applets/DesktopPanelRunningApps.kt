/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.desktoppanel.applets

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.desktoppanel.DesktopPanelIcon
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.managers.window.WindowsManager.SystemWindow
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

/**
 * Taskbar part of the panel: windows of running apps that are not
 * among the favorite apps (those indicate running state themselves).
 * Works only inside the mjdev compositor session, otherwise the
 * window list is empty and nothing renders.
 */
@Suppress("FunctionName")
@Composable
fun DesktopPanelRunningApps(
    modifier: Modifier = Modifier,
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    iconColorRunning: Color = Color.White,
    iconSize: DpSize = DpSize(56.dp, 56.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    onTooltip: (item: Any?) -> Unit = {},
    onWindowClick: (window: SystemWindow, app: App?) -> Unit = { _, _ -> },
    onWindowContextMenuClick: (window: SystemWindow, app: App?) -> Unit = { _, _ -> }
) = withDesktopScope {
    val apps = remember { allApps }
    val windows by rememberCalculated {
        val favoriteWindowIds = favoriteApps.flatMap { app ->
            windowsManager.windowsOf(app)
        }.map { window -> window.id }.toSet()
        windowsManager.windows.filter { window ->
            window.id !in favoriteWindowIds
        }
    }
    Box(
        modifier = modifier
    ) {
        LazyRow(
            modifier = Modifier.wrapContentHeight(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(windows, key = { window -> window.id }) { window ->
                val app = remember(window.windowClass) {
                    apps.firstOrNull { app ->
                        app.windowClass.isNotEmpty() &&
                                window.windowClass.contains(app.windowClass, true)
                    }
                }
                DesktopPanelIcon(
                    app = app,
                    icon = app?.name ?: window.windowClass.ifEmpty { window.title },
                    iconColor = iconColor,
                    iconBackgroundColor = iconBackgroundColor,
                    iconColorRunning = iconColorRunning,
                    iconBackgroundHover = Color.White.copy(alpha = 0.4f),
                    iconSize = iconSize,
                    iconPadding = iconPadding,
                    iconOuterPadding = iconOuterPadding,
                    runningOverride = true,
                    focusedOverride = window.focused,
                    contentDescription = window.title,
                    onToolTip = { onTooltip(app ?: window.title) },
                    onClick = { onWindowClick(window, app) },
                    onContextMenuClick = { onWindowContextMenuClick(window, app) }
                )
            }
        }
    }
}

// todo
@Preview
@Composable
fun DesktopPanelRunningAppsPreview() = preview {
    DesktopPanelRunningApps()
}

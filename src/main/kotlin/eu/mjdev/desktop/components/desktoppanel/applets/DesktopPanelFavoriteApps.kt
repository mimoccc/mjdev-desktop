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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.desktoppanel.DesktopPanelIcon
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.Custom.flowBlock
import eu.mjdev.desktop.managers.processes.ProcessManager.Companion.rememberProcessManager
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("FunctionName")
@Composable
fun DesktopPanelFavoriteApps(
    modifier: Modifier = Modifier,
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    iconColorRunning: Color = Color.White,
    iconSize: DpSize = DpSize(56.dp, 56.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    onTooltip: (item: Any?) -> Unit = {},
    onAppClick: (app: App) -> Unit = {},
    onContextMenuClick: (app: App) -> Unit = {}
) = withDesktopScope {
    val apps by flowBlock(
        emptyList(),
        processManager.size
    ) {
        appsManager.favoriteApps
    }
    Box(
        modifier = modifier
    ) {
        LazyRow(
            modifier = Modifier.wrapContentHeight(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(apps) { app ->
                DesktopPanelIcon(
                    app = app,
                    iconColor = iconColor,
                    iconBackgroundColor = iconBackgroundColor,
                    iconColorRunning = iconColorRunning,
                    iconBackgroundHover = Color.White.copy(alpha = 0.4f),
                    iconSize = iconSize,
                    iconPadding = iconPadding,
                    iconOuterPadding = iconOuterPadding,
                    onToolTip = onTooltip,
                    onClick = { onAppClick(app) },
                    onContextMenuClick = { onContextMenuClick(app) }
                )
            }
        }
    }
}

@Preview
@Composable
fun DesktopPanelFavoriteAppsPreview() = DesktopPanelFavoriteApps()

package org.mjdev.desktop.components.desktoppanel.applets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.LaunchedEffect.flowBlock
import org.mjdev.desktop.interfaces.IApp
import org.jetbrains.compose.ui.tooling.preview.Preview

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
    iconSpace: Dp = 2.dp,
    onTooltip: (item: Any?) -> Unit = {},
    onAppClick: (app: IApp) -> Unit = {},
    onContextMenuClick: (app: IApp) -> Unit = {},
) = withDesktopContext {
    val apps: List<IApp> by flowBlock(
        emptyList(),
        processManager.size
    ) {
        appsManager.favoriteApps
    }
    Box(modifier = modifier) {
        LazyRow(
            modifier = Modifier.wrapContentHeight(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(apps) { idx, app ->
                DesktopPanelIcon(
                    app = app,
                    iconColor = iconColor,
                    iconBackgroundColor = iconBackgroundColor,
                    iconColorRunning = iconColorRunning,
                    iconBackgroundHover = Color.White.copy(alpha = 0.4f),
                    iconSize = iconSize,
                    iconPadding = iconPadding,
                    iconOuterPadding = iconOuterPadding,
                    onTooltip = onTooltip,
                    onClick = {
                        onAppClick(app)
                    },
                    onContextMenuClick = { onContextMenuClick(app) }
                )
                if (idx < apps.size - 1) {
                    VerticalDivider(
                        color = Color.Transparent,
                        thickness = iconSpace
                    )
                }
            }
        }
    }
}

// todo
@Preview
@Composable
fun PreviewDesktopPanelFavoriteApps() = preview {
    DesktopPanelFavoriteApps()
}

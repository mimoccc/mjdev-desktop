package eu.mjdev.desktop.components.desktoppanel.applets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.desktoppanel.DesktopPanelIcon
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.data.App

@Composable
fun DesktopPanelFavoriteApps(
    modifier: Modifier = Modifier,
    api: DesktopProvider = LocalDesktop.current,
    favoriteApps: State<List<App>> = api.appsProvider.favoriteApps.collectAsState(emptyList()),
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    iconSize: DpSize = DpSize(56.dp, 56.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    onTooltip: (item: Any?) -> Unit = {},
    onClick: (app: App) -> Unit = { app -> app.start() },
) = Box(
    modifier = modifier
) {
    LazyRow(
        modifier = Modifier.wrapContentHeight(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(favoriteApps.value) { app ->
            DesktopPanelIcon(
                app = app,
                iconColor = iconColor,
                iconBackgroundColor = iconBackgroundColor,
                iconBackgroundHover = Color.White.copy(alpha = 0.4f),
                iconSize = iconSize,
                iconPadding = iconPadding,
                iconOuterPadding = iconOuterPadding,
                onToolTip = onTooltip,
                onClick = { onClick(app) }
            )
        }
    }
}
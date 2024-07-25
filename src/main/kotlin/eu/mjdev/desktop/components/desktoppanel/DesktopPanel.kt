package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.shadow.TopShadow
import eu.mjdev.desktop.components.slidemenu.SlidingMenu
import eu.mjdev.desktop.components.slidemenu.VisibilityState
import eu.mjdev.desktop.components.slidemenu.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.data.App

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName")
@Composable
fun DesktopPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.SuperDarkGray,
    shadowColor: Color = backgroundColor,
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    dividerWidth: Dp = 4.dp,
    iconSize: DpSize = DpSize(48.dp, 48.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    panelAlpha: Float = 0.7f,
    showMenuIcon: Boolean = true,
    state: VisibilityState = rememberVisibilityState(),
    onVisibilityChange: (visible: Boolean) -> Unit = {},
    onMenuIconClicked: () -> Unit = {},
    api: DesktopProvider = LocalDesktop.current
) = SlidingMenu(
    modifier = modifier,
    orientation = Orientation.Vertical,
    state = state,
    onVisibilityChange = onVisibilityChange
) { isVisible ->
    val favoriteApps = api.appsProvider.favoriteApps.collectAsState(emptyList())
    val tooltipData: MutableState<Any?> = mutableStateOf(null)
    val onToolTip: (item: Any?) -> Unit = { item -> tooltipData.value = item }
    if (!isVisible) {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerWidth),
            color = Color.Transparent,
            thickness = dividerWidth
        )
    }
    AnimatedVisibility(
        isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        TooltipArea(
            tooltip = {
                // todo more types
                when (tooltipData.value) {
                    is App -> DesktopPanelAppTooltip(tooltipData.value as App)
                    else -> {}
                }
            },
            modifier = Modifier.fillMaxWidth(),
            tooltipPlacement = TooltipPlacement.CursorPoint(
                alignment = Alignment.TopEnd,
                offset = DpOffset(16.dp, 32.dp)
            )
        ) {
            TopShadow(
                modifier = Modifier.fillMaxWidth(),
                color = shadowColor,
                contentBackgroundColor = backgroundColor.copy(alpha = panelAlpha)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                backgroundColor.copy(alpha = 0.3f),
                                backgroundColor.copy(alpha = 0.7f),
                                backgroundColor.copy(alpha = 0.3f),
                            )
                        )
                    ),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showMenuIcon) {
                        item {
                            DesktopPanelIcon(
                                icon = "menu",
                                iconColor = iconColor,
                                iconBackgroundColor = iconBackgroundColor,
                                iconBackgroundHover = Color.White.copy(alpha = 0.3f),
                                iconSize = iconSize,
                                iconPadding = iconPadding,
                                onToolTip = onToolTip,
                                onClick = {
                                    onMenuIconClicked()
                                }
                            )
                            Divider(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .height(iconSize.height - 8.dp)
                                    .width(2.dp)
                                    .background(Color.White.copy(alpha = 0.4f))
                            )
                        }
                    }
                    items(favoriteApps.value) { app ->
                        DesktopPanelIcon(
                            app = app,
                            iconColor = iconColor,
                            iconBackgroundColor = iconBackgroundColor,
                            iconBackgroundHover = Color.White.copy(alpha = 0.3f),
                            iconSize = iconSize,
                            iconPadding = iconPadding,
                            onToolTip = onToolTip
                        )
                    }
                    item {
                        DesktopPanelText(
                            text = api.appsProvider.currentLocale.country,
                            backgroundHover = Color.White.copy(alpha = 0.3f),
                            onToolTip = onToolTip
                        )
                    }
                }
            }
        }
    }
}

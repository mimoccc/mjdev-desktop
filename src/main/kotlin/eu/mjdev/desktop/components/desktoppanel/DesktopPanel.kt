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
import androidx.compose.runtime.*
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
import eu.mjdev.desktop.extensions.Compose.height
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.data.App
import eu.mjdev.desktop.windows.TopWindow
import eu.mjdev.desktop.windows.TopWindowState
import eu.mjdev.desktop.windows.TopWindowState.Companion.rememberTopWindowState
import eu.mjdev.desktop.windows.WindowBounds

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName")
@Composable
fun DesktopPanel(
    modifier: Modifier = Modifier,
    api: DesktopProvider = LocalDesktop.current,
    backgroundColor: Color = api.currentUser.theme.backgroundColor,
    shadowColor: Color = backgroundColor,
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    dividerWidth: Dp = 4.dp,
    iconSize: DpSize = DpSize(56.dp, 56.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    panelAlpha: Float = 0.7f,
    panelHeight: Dp = iconSize.height + iconPadding.height + iconOuterPadding.height,
    showMenuIcon: Boolean = true,
    panelState: VisibilityState = rememberVisibilityState(),
    enter: EnterTransition = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exit: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    menuState: VisibilityState = rememberVisibilityState(),
    windowState: TopWindowState = rememberTopWindowState(
        position = api.currentUser.theme.panelLocation.alignment,
        size = api.containerSize.copy(height = panelHeight),
        exit = exit,
        enter = enter,
        computeBounds = { isVisible ->
            val containerHeight = api.containerSize.height
            val containerWidth = api.containerSize.width
            WindowBounds(
                0.dp,
                if (isVisible) (containerHeight - panelHeight) else (containerHeight - dividerWidth),
                containerWidth,
                if (isVisible) panelHeight else dividerWidth
            )
        }
    ),
    favoriteApps: State<List<App>> = api.appsProvider.favoriteApps.collectAsState(emptyList()),
    tooltipData: MutableState<Any?> = mutableStateOf(null),
    onToolTip: (item: Any?) -> Unit = { item -> tooltipData.value = item },
    onVisibilityChange: (visible: Boolean) -> Unit = {},
    onMenuIconClicked: () -> Unit,
) = TopWindow(
    windowState = windowState,
    onFocusChange = { hasFocus ->
        if (!hasFocus && !menuState.isVisible) panelState.hide()
    }
) {
    SlidingMenu(
        modifier = modifier,
        orientation = Orientation.Vertical,
        state = panelState,
        onVisibilityChange = onVisibilityChange
    ) { isVisible ->
        if (!isVisible) {
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dividerWidth),
                color = Color.Transparent,
                thickness = dividerWidth
            )
        }
        TooltipArea(
            tooltip = {
                // todo more types
                when (tooltipData.value) {
                    is App -> DesktopPanelAppTooltip(tooltipData.value as App)
                    else -> {}
                }
            },
            modifier = Modifier.fillMaxSize(),
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
                    modifier = Modifier.fillMaxSize().background(
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
                                iconOuterPadding = iconOuterPadding,
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
                            iconOuterPadding = iconOuterPadding,
                            onToolTip = onToolTip,
                            onClick = {
                                app.start()
                            }
                        )
                    }
                    item {
                        DesktopPanelText(
                            text = api.appsProvider.currentLocale.country,
                            backgroundHover = Color.White.copy(alpha = 0.3f),
                            onToolTip = onToolTip,
                            onClick = {
                                // todo
                            }
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(
        panelState.isVisible,
        api.currentUser.theme.panelHideDelay
    ) {
        panelState.enabled = api.currentUser.theme.panelHideDelay > 0
        windowState.isVisible = api.currentUser.theme.panelHideDelay < 1 || panelState.isVisible
    }
}

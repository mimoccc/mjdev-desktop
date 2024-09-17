package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.desktoppanel.applets.DesktopMenuIcon
import eu.mjdev.desktop.components.desktoppanel.applets.DesktopPanelFavoriteApps
import eu.mjdev.desktop.components.desktoppanel.applets.DesktopPanelLanguage
import eu.mjdev.desktop.components.sliding.SlidingMenu
import eu.mjdev.desktop.components.sliding.VisibilityState
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.Compose.height
import eu.mjdev.desktop.extensions.Modifier.topShadow
import eu.mjdev.desktop.helpers.animation.Animations.DesktopPanelEnterAnimation
import eu.mjdev.desktop.helpers.animation.Animations.DesktopPanelExitAnimation
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.windows.ChromeWindow

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName")
@Composable
fun DesktopPanel(
    api: DesktopProvider = LocalDesktop.current,
    iconColor: Color = Color.Black,
    iconColorRunning: Color = Color.White.copy(0.8f),
    iconBackgroundColor: Color = Color.White.copy(0.6f),
    dividerWidth: Dp = 4.dp,
    iconSize: DpSize = DpSize(56.dp, 56.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    showMenuIcon: Boolean = true,
    panelContentPadding: PaddingValues = PaddingValues(4.dp),
    panelState: VisibilityState = rememberVisibilityState(),
    enterAnimation: EnterTransition = DesktopPanelEnterAnimation,
    exitAnimation: ExitTransition = DesktopPanelExitAnimation,
    tooltipHeight: Dp = 64.dp,
    tooltipData: MutableState<Any?> = remember { mutableStateOf(null) },
    onTooltip: (item: Any?) -> Unit = { item -> tooltipData.value = item },
    panelHeight: (visible: Boolean) -> Dp = { visible ->
        if (visible)
            iconSize.height + iconOuterPadding.height + tooltipHeight + panelContentPadding.height
        else
            dividerWidth
    },
    tooltipConverter: (Any?) -> TooltipData = { item ->
        when (item) {
            is App -> TooltipData(title = item.name, description = item.comment)
            else -> TooltipData(title = item.toString())
        }
    },
    onMenuIconClicked: () -> Unit = {
        // todo
    },
    onMenuIconContextMenuClicked: () -> Unit = {
        // todo
    },
    onFocusChange: (Boolean) -> Unit = {},
    onAppClick: (App) -> Unit = { app -> api.appsProvider.startApp(app) },
    onAppContextMenuClick: (App) -> Unit = {
        // todo
    },
    onLanguageClick: () -> Unit = {
        // todo
    },
) {
    val backgroundColor by remember { api.currentUser.theme.backgroundColorState }
    ChromeWindow(
        visible = true,
        enterAnimation = enterAnimation,
        exitAnimation = exitAnimation,
        position = WindowPosition.Aligned(Alignment.BottomCenter),
        size = DpSize(
            api.containerSize.width,
            panelHeight(panelState.isVisible)
        ),
        onFocusChange = { focused ->
            panelState.onFocusChange(focused)
            onFocusChange(focused)
        }
    ) {
        SlidingMenu(
            modifier = Modifier.fillMaxWidth(),
            orientation = Vertical,
            state = panelState,
        ) { isVisible ->
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dividerWidth),
                color = Color.Transparent,
                thickness = dividerWidth
            )
            if (isVisible) {
                TooltipArea(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    tooltip = {
                        DesktopPanelTooltip(
                            tooltipData.value,
                            converter = tooltipConverter
                        )
                    },
                    tooltipPlacement = TooltipPlacement.CursorPoint(
                        alignment = Alignment.TopEnd,
                        offset = DpOffset(32.dp, 32.dp)
                    )
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tooltipHeight)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            backgroundColor.copy(alpha = 0.3f),
                                            backgroundColor.copy(alpha = 0.7f),
                                            backgroundColor.copy(alpha = 0.8f),
                                        )
                                    )
                                )
                                .topShadow(
                                    color = backgroundColor.copy(alpha = 0.3f),
                                    offsetY = 4.dp,
                                    blur = 10.dp
                                )
                                .onPlaced(panelState::onPlaced),
                        ) {
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                                color = Color.White.copy(0.1f),
                                thickness = 2.dp
                            )
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .padding(panelContentPadding)
                            ) {
                                if (showMenuIcon) {
                                    DesktopMenuIcon(
                                        modifier = Modifier.align(Alignment.CenterStart),
                                        iconColor = iconColor,
                                        iconBackgroundColor = iconBackgroundColor,
                                        iconSize = iconSize,
                                        iconPadding = iconPadding,
                                        iconOuterPadding = iconOuterPadding,
                                        onTooltip = onTooltip,
                                        onClick = { onMenuIconClicked() },
                                        onContextMenuClick = onMenuIconContextMenuClicked
                                    )
                                }
                                DesktopPanelFavoriteApps(
                                    modifier = Modifier.align(Alignment.Center),
                                    iconColor = iconColor,
                                    iconBackgroundColor = iconBackgroundColor,
                                    iconColorRunning = iconColorRunning,
                                    iconSize = iconSize,
                                    iconPadding = iconPadding,
                                    iconOuterPadding = iconOuterPadding,
                                    onTooltip = onTooltip,
                                    onAppClick = onAppClick,
                                    onContextMenuClick = onAppContextMenuClick
                                )
                                DesktopPanelLanguage(
                                    modifier = Modifier.align(Alignment.CenterEnd),
                                    onTooltip = onTooltip,
                                    onClick = onLanguageClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
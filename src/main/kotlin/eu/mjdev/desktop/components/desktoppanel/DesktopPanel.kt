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
import eu.mjdev.desktop.components.tooltip.Tooltip
import eu.mjdev.desktop.components.tooltip.TooltipData
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.ColorUtils.lighter
import eu.mjdev.desktop.extensions.Compose.height
import eu.mjdev.desktop.extensions.Modifier.topShadow
import eu.mjdev.desktop.helpers.animation.Animations.DesktopPanelEnterAnimation
import eu.mjdev.desktop.helpers.animation.Animations.DesktopPanelExitAnimation
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.DesktopProvider.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindow
import eu.mjdev.desktop.windows.ChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName")
@Composable
fun DesktopPanel(
    api: DesktopProvider = LocalDesktop.current,
    dividerWidth: Dp = 4.dp,
    iconSize: DpSize = DpSize(56.dp, 56.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    showMenuIcon: Boolean = true,
    panelContentPadding: PaddingValues = PaddingValues(4.dp),
    panelState: VisibilityState = rememberVisibilityState(),
    menuState: VisibilityState = rememberVisibilityState(),
    enterAnimation: EnterTransition = DesktopPanelEnterAnimation,
    exitAnimation: ExitTransition = DesktopPanelExitAnimation,
    tooltipHeight: Dp = 64.dp,
    tooltipConverter: (Any?) -> TooltipData = { item ->
        when (item) {
            is App -> TooltipData(title = item.name, description = item.comment)
            else -> TooltipData(title = item.toString())
        }
    },
    position: WindowPosition.Aligned = WindowPosition.Aligned(Alignment.BottomCenter),
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
) = withDesktopScope {
    var tooltipState = remember { mutableStateOf<Any?>(null) }
    val panelHeight: (visible: Boolean) -> Dp = { visible ->
        if (visible)
            iconSize.height + iconOuterPadding.height + tooltipHeight + panelContentPadding.height
        else
            dividerWidth
    }
    val size = remember(panelState.isVisible) {
        DpSize(
            api.containerSize.width,
            panelHeight(panelState.isVisible)
        )
    }
    val windowState: ChromeWindowState = rememberChromeWindowState(position = position, size = size)
    ChromeWindow(
        windowState = windowState,
        visible = true,
        enterAnimation = enterAnimation,
        exitAnimation = exitAnimation,
        onFocusChange = { focused ->
            panelState.onFocusChange(focused)
            onFocusChange(focused)
        }
    ) {
        SlidingMenu(
            modifier = Modifier.fillMaxWidth(),
            orientation = Vertical,
            state = panelState,
            onPointerEnter = {
                panelState.show()
                if (!menuState.isVisible) {
                    windowState.requestFocus()
                }
            },
            onPointerLeave = {
                if (!menuState.isVisible) {
                    panelState.hide()
                }
            }
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
                        Tooltip(
                            textColor = textColor.value,
                            borderColor = borderColor.value,
                            state = tooltipState,
                            converter = tooltipConverter,
                            backgroundColor = backgroundColor.value
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
                                            backgroundColor.value.copy(alpha = 0.3f),
                                            backgroundColor.value.copy(alpha = 0.7f),
                                            backgroundColor.value.copy(alpha = 0.8f),
                                        )
                                    )
                                )
                                .topShadow(
                                    color = borderColor.value.alpha(0.3f),
                                    blur = 4.dp
                                )
                                .onPlaced(panelState::onPlaced),
                        ) {
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                                color = borderColor.value,
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
                                        iconColor = borderColor.value,
                                        iconBackgroundColor = iconsTintColor.value,
                                        iconSize = iconSize,
                                        iconPadding = iconPadding,
                                        iconOuterPadding = iconOuterPadding,
                                        onTooltip = { item -> tooltipState.value = item },
                                        onClick = { onMenuIconClicked() },
                                        onContextMenuClick = onMenuIconContextMenuClicked
                                    )
                                }
                                DesktopPanelFavoriteApps(
                                    modifier = Modifier.align(Alignment.Center),
                                    iconColor = borderColor.value,
                                    iconBackgroundColor = iconsTintColor.value,
                                    iconColorRunning = iconsTintColor.value.lighter(0.3f),
                                    iconSize = iconSize,
                                    iconPadding = iconPadding,
                                    iconOuterPadding = iconOuterPadding,
                                    onTooltip = { item -> tooltipState.value = item },
                                    onAppClick = onAppClick,
                                    onContextMenuClick = onAppContextMenuClick
                                )
                                DesktopPanelLanguage(
                                    modifier = Modifier.align(Alignment.CenterEnd),
                                    onTooltip = { item -> tooltipState.value = item },
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
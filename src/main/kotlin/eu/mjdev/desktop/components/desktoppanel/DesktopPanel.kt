package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.blur.BlurPanel
import eu.mjdev.desktop.components.desktoppanel.applets.DesktopMenuIcon
import eu.mjdev.desktop.components.desktoppanel.applets.DesktopPanelDateTime
import eu.mjdev.desktop.components.desktoppanel.applets.DesktopPanelFavoriteApps
import eu.mjdev.desktop.components.desktoppanel.applets.DesktopPanelLanguage
import eu.mjdev.desktop.components.sliding.SlidingMenu
import eu.mjdev.desktop.components.tooltip.Tooltip
import eu.mjdev.desktop.components.tooltip.TooltipState
import eu.mjdev.desktop.components.tooltip.rememberTooltipState
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.ColorUtils.lighter
import eu.mjdev.desktop.extensions.Compose.height
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.extensions.Modifier.topShadow
import eu.mjdev.desktop.helpers.animation.Animations.DesktopPanelEnterAnimation
import eu.mjdev.desktop.helpers.animation.Animations.DesktopPanelExitAnimation
import eu.mjdev.desktop.helpers.internal.KeyEventHandler.Companion.globalKeyEventHandler
import eu.mjdev.desktop.provider.DesktopScope
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindow
import eu.mjdev.desktop.windows.ChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName")
@Composable
fun DesktopPanel(
    iconSize: DpSize = DpSize(56.dp, 56.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    showMenuIcon: Boolean = true,
    panelState: ChromeWindowState = rememberChromeWindowState(),
    menuState: ChromeWindowState = rememberChromeWindowState(),
    tooltipState: TooltipState = rememberTooltipState(),
    enterAnimation: EnterTransition = DesktopPanelEnterAnimation,
    exitAnimation: ExitTransition = DesktopPanelExitAnimation,
    position: WindowPosition.Aligned = WindowPosition.Aligned(Alignment.BottomCenter),
    onMenuIconClicked: () -> Unit = {},
    onMenuIconContextMenuClicked: () -> Unit = {},
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    onAppClick: DesktopScope.(App) -> Unit = { app ->
        app.start()
    },
    onAppContextMenuClick: (App) -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onClockClick: () -> Unit = {}
) = withDesktopScope {
    val panelDividerWidth by rememberState(theme.panelDividerWidth)
    val panelHeight: (visible: Boolean) -> Dp = { visible ->
        if (visible) {
            iconSize.height + iconOuterPadding.height + tooltipState.tooltipHeight +
                    theme.panelContentPadding.times(2) + 8.dp
        } else panelDividerWidth
    }
    val panelSize = remember(panelState.isVisible, panelState.enabled) {
        DpSize(containerSize.width, panelHeight(panelState.isVisible))
    }
    val windowState: ChromeWindowState = rememberChromeWindowState(
        position = position, size = panelSize
    )
    panelState.updateSize(panelSize - DpSize(0.dp, tooltipState.tooltipHeight))
    globalKeyEventHandler(
        isEnabled = { panelState.isVisible && panelState.enabled }
    ) {
        onEscape {
            panelState.hide()
            true
        }
    }
    ChromeWindow(
        windowState = windowState,
        visible = true,
        alwaysOnTop = panelState.enabled,
        enterAnimation = enterAnimation,
        exitAnimation = exitAnimation,
        onFocusChange = onFocusChange
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
            }) { isVisible ->
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .height(panelDividerWidth),
                color = Color.Transparent,
                thickness = panelDividerWidth
            )
            if (isVisible) {
                TooltipArea(
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight(),
                    tooltip = {
                        Tooltip(
                            tooltipState = tooltipState,
                        )
                    },
                    tooltipPlacement = TooltipPlacement.CursorPoint(
                        alignment = Alignment.TopEnd,
                        offset = DpOffset(16.dp, 64.dp)
                    )
                ) {
                    Column {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .height(tooltipState.tooltipHeight)
                        )
                        BlurPanel(
                            modifier = Modifier.fillMaxWidth()
                                .wrapContentHeight()
                                .clip(RoundedCornerShape(panelSize.height.div(2)))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .wrapContentHeight()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                backgroundColor.alpha(0.2f),
                                                backgroundColor.alpha(0.4f),
                                                backgroundColor.alpha(0.5f),
                                            )
                                        )
                                    )
                                    .clip(RoundedCornerShape(panelSize.height.div(2)))
                                    .topShadow(
                                        color = borderColor.alpha(0.3f),
                                        blur = 4.dp
                                    ).padding(
                                        start = 4.dp,
                                        end = 4.dp,
                                        bottom = 4.dp
                                    ).onPlaced(panelState::onPlaced),
                            ) {
                                Divider(
                                    modifier = Modifier.fillMaxWidth()
                                        .height(2.dp),
                                    color = borderColor,
                                    thickness = 2.dp
                                )
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .padding(theme.panelContentPadding)
                                ) {
                                    if (showMenuIcon) {
                                        DesktopMenuIcon(
                                            modifier = Modifier.align(Alignment.CenterStart),
                                            iconColor = borderColor,
                                            iconBackgroundColor = iconsTintColor,
                                            iconSize = iconSize,
                                            iconPadding = iconPadding,
                                            iconOuterPadding = iconOuterPadding,
                                            onTooltip = { item -> tooltipState.show(item) },
                                            onClick = { onMenuIconClicked() },
                                            onContextMenuClick = onMenuIconContextMenuClicked
                                        )
                                    }
                                    DesktopPanelFavoriteApps(
                                        modifier = Modifier.align(Alignment.Center),
                                        iconColor = borderColor,
                                        iconBackgroundColor = iconsTintColor,
                                        iconColorRunning = iconsTintColor.lighter(0.3f),
                                        iconSize = iconSize,
                                        iconPadding = iconPadding,
                                        iconOuterPadding = iconOuterPadding,
                                        onTooltip = { item -> tooltipState.show(item) },
                                        onAppClick = { app -> onAppClick(app) },
                                        onContextMenuClick = onAppContextMenuClick
                                    )
                                    DesktopPanelTray(
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                            .padding(end = 16.dp),
                                    ) {
                                        DesktopPanelLanguage(
                                            onTooltip = { item -> tooltipState.show(item) },
                                            onClick = onLanguageClick
                                        )
                                        DesktopPanelDateTime(
                                            onTooltip = { item -> tooltipState.show(item) },
                                            onClick = onClockClick
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun DesktopPanelPreview() = DesktopPanel()

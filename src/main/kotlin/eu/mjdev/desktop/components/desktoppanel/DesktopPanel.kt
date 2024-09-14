package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import eu.mjdev.desktop.components.shadow.TopShadow
import eu.mjdev.desktop.components.sliding.SlidingMenu
import eu.mjdev.desktop.components.sliding.VisibilityState
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.height
import eu.mjdev.desktop.helpers.DpBounds.Companion.toDpBounds
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.data.App
import eu.mjdev.desktop.windows.ChromeWindow

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName")
@Composable
fun DesktopPanel(
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
    showMenuIcon: Boolean = true,
    panelState: VisibilityState = rememberVisibilityState(),
    enterAnimation: EnterTransition = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exitAnimation: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    menuState: VisibilityState = rememberVisibilityState(),
    tooltipData: MutableState<Any?> = mutableStateOf(null),
    onTooltip: (item: Any?) -> Unit = { item -> tooltipData.value = item },
    panelHeight: (visible: Boolean) -> Dp = { visible ->
        if (visible) iconSize.height + iconOuterPadding.height else dividerWidth
    },
    onMenuIconClicked: () -> Unit = {},
) = ChromeWindow(
    visible = true,
    enterAnimation = enterAnimation,
    exitAnimation = exitAnimation,
    position = WindowPosition.Aligned(Alignment.BottomCenter),
    size = DpSize(
        api.containerSize.width,
        panelHeight(panelState.isVisible)
    ),
    onFocusChange = { focus -> if (!focus && !menuState.isVisible) panelState.hide() }
) {
    SlidingMenu(
        modifier = Modifier.fillMaxWidth(),
        orientation = Vertical,
        state = panelState,
    ) { isVisible ->
        if (!isVisible) {
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dividerWidth),
                color = Color.Transparent,
                thickness = dividerWidth
            )
        } else {
            TooltipArea(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                tooltip = {
                    // todo more types
                    when (tooltipData.value) {
                        is App -> DesktopPanelAppTooltip(tooltipData.value as App)
                        else -> {}
                    }
                },
                tooltipPlacement = TooltipPlacement.CursorPoint(
                    alignment = Alignment.TopEnd,
                    offset = DpOffset(32.dp, 32.dp)
                )
            ) {
                TopShadow(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().onPlaced {
                        panelState.bounds = it.toDpBounds()
                    },
                    color = shadowColor,
                    contentBackgroundColor = backgroundColor.copy(alpha = panelAlpha)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight().background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    backgroundColor.copy(alpha = 0.3f),
                                    backgroundColor.copy(alpha = 0.7f),
                                    backgroundColor.copy(alpha = 0.3f),
                                )
                            )
                        )
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
                                onClick = onMenuIconClicked
                            )
                        }
                        DesktopPanelFavoriteApps(
                            modifier = Modifier.align(Alignment.Center),
                            iconColor = iconColor,
                            iconBackgroundColor = iconBackgroundColor,
                            iconSize = iconSize,
                            iconPadding = iconPadding,
                            iconOuterPadding = iconOuterPadding,
                            onTooltip = onTooltip
                        )
                        DesktopPanelLanguage(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            onTooltip = onTooltip,
                            onClick = {
                                // todo
                            }
                        )
                    }
                }
            }
        }
    }
}

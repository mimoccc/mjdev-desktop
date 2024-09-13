package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.shadow.TopShadow
import eu.mjdev.desktop.components.sliding.VisibilityState
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.helpers.DpBounds.Companion.toDpBounds
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.data.App

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
//    dividerWidth: Dp = 4.dp,
    iconSize: DpSize = DpSize(56.dp, 56.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    panelAlpha: Float = 0.7f,
    showMenuIcon: Boolean = true,
    panelState: VisibilityState = rememberVisibilityState(),
//    enterAnimation: EnterTransition = fadeIn() + slideInVertically(initialOffsetY = { it }),
//    exitAnimation: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    favoriteApps: State<List<App>> = api.appsProvider.favoriteApps.collectAsState(emptyList()),
    tooltipData: MutableState<Any?> = mutableStateOf(null),
    onToolTip: (item: Any?) -> Unit = { item -> tooltipData.value = item },
//    windowState: ChromeWindowState = rememberChromeWindowState(
//        position = WindowPosition.Aligned(Alignment.BottomCenter),
//        size = DpSize(api.containerSize.width, Dp.Unspecified)
//    ),
    onMenuIconClicked: () -> Unit = {},
) = Box(
    modifier = modifier
) {
//    ChromeWindow(
//    windowState = windowState,
//    enterAnimation = enterAnimation,
//    exitAnimation = exitAnimation
//) {
//    SlidingMenu(
//        modifier = modifier,
//        orientation = Orientation.Vertical,
//        state = panelState,
//    ) { isVisible ->
//        if (!isVisible) {
//            Divider(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(dividerWidth),
//                color = Color.Red,
//                thickness = dividerWidth
//            )
//        } else {
    TooltipArea(
        modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(top = 32.dp),
        tooltip = {
            // todo more types
            when (tooltipData.value) {
                is App -> DesktopPanelAppTooltip(tooltipData.value as App)
                else -> {}
            }
        },
        tooltipPlacement = TooltipPlacement.CursorPoint(
            alignment = Alignment.TopEnd,
            offset = DpOffset(16.dp, 32.dp)
        )
    ) {
        TopShadow(
            modifier = Modifier.fillMaxWidth().wrapContentHeight().onPlaced {
                panelState.bounds = it.toDpBounds()
            },
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
//        }
//    }
//}

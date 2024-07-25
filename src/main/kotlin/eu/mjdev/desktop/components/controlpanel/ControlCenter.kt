package eu.mjdev.desktop.components.controlpanel

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import eu.mjdev.desktop.components.shadow.LeftShadow
import eu.mjdev.desktop.components.shadow.RightShadow
import eu.mjdev.desktop.components.slidemenu.SlidingMenu
import eu.mjdev.desktop.components.slidemenu.VisibilityState
import eu.mjdev.desktop.components.slidemenu.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.components.window.TopWindow
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Compose.setWindowBounds
import eu.mjdev.desktop.helpers.WindowFocusState.Companion.windowFocusHandler
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("FunctionName")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlCenter(
    api: DesktopProvider = LocalDesktop.current,
    backgroundColor: Color = api.currentUser.theme.backgroundColor,
    backgroundAlpha: Float = api.currentUser.theme.controlCenterBackgroundAlpha,
    shadowColor: Color = backgroundColor, // todo theme
    controlCenterDividerColor: Color = backgroundColor,// todo theme
    controlCenterExpandedWidth: Dp = api.currentUser.theme.controlCenterExpandedWidth,
    controlCenterDividerWidth: Dp = api.currentUser.theme.controlCenterDividerWidth,
    controlCenterIconColor: Color = api.currentUser.theme.controlCenterIconColor,
    controlCenterIconSize: DpSize = api.currentUser.theme.controlCenterIconSize,
    pages: List<ControlCenterPage> = api.controlCenterPages,
    pagerState: PagerState = rememberPagerState(pageCount = { pages.size }, initialPage = 0),
    controlCenterState: VisibilityState = rememberVisibilityState(),
    scope: CoroutineScope = rememberCoroutineScope(),
    onVisibilityChange: (visible: Boolean) -> Unit = {},
) {
    val windowState: WindowState = rememberWindowState(
        placement = WindowPlacement.Floating,
        position = WindowPosition.Aligned(api.currentUser.theme.controlCenterLocation.alignment),
        size = api.containerSize.copy(width = controlCenterDividerWidth),
        isMinimized = false
    )
    TopWindow(
        windowState = windowState,
    ) {
        windowFocusHandler { hasFocus -> if (!hasFocus) controlCenterState.hide() }
        SlidingMenu(
            orientation = Orientation.Horizontal,
            state = controlCenterState,
            onVisibilityChange = onVisibilityChange
        ) { isVisible ->
            if (!isVisible) {
                Divider(
                    modifier = Modifier.fillMaxHeight().width(controlCenterDividerWidth),
                    color = Color.Transparent,
                    thickness = controlCenterDividerWidth
                )
            }
            AnimatedVisibility(
                isVisible,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            ) {
                LeftShadow(
                    modifier = Modifier.fillMaxHeight().wrapContentSize(),
                    color = shadowColor,
                    contentBackgroundColor = backgroundColor.copy(alpha = backgroundAlpha)
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight().wrapContentSize(),
                    ) {
                        Divider(
                            modifier = Modifier.fillMaxHeight().width(controlCenterDividerWidth),
                            color = controlCenterDividerColor,
                            thickness = controlCenterDividerWidth
                        )
                        Box(
                            contentAlignment = Alignment.TopEnd
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(backgroundColor)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.Start
                            ) {
                                itemsIndexed(pages) { idx, page ->
                                    val isSelected = (pagerState.currentPage == idx)
                                    Icon(
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .size(controlCenterIconSize)
                                            .background(
                                                color = if (isSelected) Color.White.copy(alpha = 0.4f) else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                scope.launch {
                                                    pagerState.scrollToPage(idx)
                                                }
                                            },
                                        imageVector = page.icon,
                                        contentDescription = "",
                                        tint = if (isSelected) backgroundColor else controlCenterIconColor
                                    )
                                }
                            }
                            RightShadow(
                                modifier = Modifier.padding(
                                    end = controlCenterIconSize.width + 8.dp
                                ).fillMaxHeight().wrapContentSize(),
                                color = Color.Black,
                                alpha = 0.3f,
                                contentBackgroundColor = Color.Transparent
                            ) {
                                Row {
                                    HorizontalPager(
                                        modifier = Modifier.width(controlCenterExpandedWidth),
                                        state = pagerState
                                    ) { page ->
                                        pages[page].content(backgroundColor)
                                    }
                                    Divider(
                                        modifier = Modifier.fillMaxHeight().width(controlCenterDividerWidth),
                                        color = controlCenterDividerColor,
                                        thickness = controlCenterDividerWidth
                                    )
                                }
                            }
                            Divider(
                                modifier = Modifier.fillMaxHeight().width(controlCenterDividerWidth),
                                color = controlCenterDividerColor,
                                thickness = controlCenterDividerWidth
                            )
                        }
                    }
                }
            }
        }
        launchedEffect(controlCenterState.isVisible) { isVisible ->
            val x = when (isVisible) {
                true -> (api.containerSize.width - controlCenterExpandedWidth)
                else -> (api.containerSize.width - controlCenterDividerWidth)
            }
            val y = 0.dp
            val width = when (isVisible) {
                true -> controlCenterExpandedWidth
                else -> controlCenterDividerWidth
            }
            val height = api.containerSize.height
            window.setWindowBounds(x, y, width, height)
        }
    }
}
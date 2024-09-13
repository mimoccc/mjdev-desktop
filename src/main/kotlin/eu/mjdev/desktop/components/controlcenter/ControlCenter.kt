package eu.mjdev.desktop.components.controlcenter

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.shadow.LeftShadow
import eu.mjdev.desktop.components.shadow.RightShadow
import eu.mjdev.desktop.components.sliding.VisibilityState
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.windows.ChromeWindow
import eu.mjdev.desktop.windows.ChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("FunctionName")
@Composable
fun ControlCenter(
    modifier: Modifier = Modifier,
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
    windowState: ChromeWindowState = rememberChromeWindowState(
        position = WindowPosition.Aligned(Alignment.TopEnd),
        size = DpSize(api.containerSize.width, Dp.Unspecified)
    ),
    enterAnimation: EnterTransition = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
    exitAnimation: ExitTransition = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
) = ChromeWindow(
    visible = controlCenterState.isVisible,
    windowState = windowState,
    enterAnimation = enterAnimation,
    exitAnimation = exitAnimation,
    size = if (controlCenterState.isVisible) DpSize(controlCenterExpandedWidth, api.containerSize.height)
    else DpSize(controlCenterDividerWidth, api.containerSize.height)
) {
//    SlidingMenu(
//        modifier = modifier,
//        orientation = Orientation.Horizontal,
//        state = controlCenterState,
//    ) { isVisible ->
//        if (!isVisible) {
//            Divider(
//                modifier = Modifier.fillMaxHeight().width(controlCenterDividerWidth),
//                color = Color.Transparent,
//                thickness = controlCenterDividerWidth
//            )
//        } else {
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
                                    color = if (isSelected) Color.White else Color.Transparent,
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
//    }
//}
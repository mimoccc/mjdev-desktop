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
import eu.mjdev.desktop.components.SlideMenuState
import eu.mjdev.desktop.components.SlideMenuState.Companion.rememberSlideMenuState
import eu.mjdev.desktop.components.SlidingMenu
import eu.mjdev.desktop.components.shadow.LeftShadow
import eu.mjdev.desktop.components.shadow.RightShadow
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("FunctionName")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.SuperDarkGray,
    backgroundAlpha: Float = 0.8f,
    shadowColor: Color = backgroundColor,
    controlCenterDividerColor: Color = backgroundColor,
    controlCenterExpandedWidth: Dp,
    controlCenterDividerWidth: Dp,
    controlCenterIconColor: Color,
    controlCenterIconSize: DpSize,
    pages: List<ControlCenterPage>,
    pagerState: PagerState = rememberPagerState(pageCount = { pages.size }),
    state: SlideMenuState = rememberSlideMenuState(),
    scope: CoroutineScope = rememberCoroutineScope(),
    onVisibilityChange: (visible: Boolean) -> Unit = {},
) = SlidingMenu(
    modifier = modifier,
    orientation = Orientation.Horizontal,
    state = state,
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
            modifier = modifier.fillMaxHeight().wrapContentSize(),
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
                        modifier = modifier.padding(
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

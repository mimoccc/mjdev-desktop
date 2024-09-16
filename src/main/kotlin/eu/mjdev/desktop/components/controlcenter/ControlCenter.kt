package eu.mjdev.desktop.components.controlcenter

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage.Companion.rememberControlCenterScope
import eu.mjdev.desktop.components.shadow.LeftShadow
import eu.mjdev.desktop.components.shadow.RightShadow
import eu.mjdev.desktop.components.sliding.SlidingMenu
import eu.mjdev.desktop.components.sliding.VisibilityState
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.windows.ChromeWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("FunctionName")
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
    pagerState: MutableState<Int> = remember { mutableStateOf(0) },
    controlCenterState: VisibilityState = rememberVisibilityState(),
    scope: CoroutineScope = rememberCoroutineScope(),
    enterAnimation: EnterTransition = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
    exitAnimation: ExitTransition = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
    onFocusChange: (Boolean) -> Unit = {},
) = ChromeWindow(
    visible = true,
    enterAnimation = enterAnimation,
    exitAnimation = exitAnimation,
    position = WindowPosition.Aligned(Alignment.TopEnd),
    size = if (controlCenterState.isVisible) {
        DpSize(controlCenterExpandedWidth, api.containerSize.height)
    } else {
        DpSize(controlCenterDividerWidth, api.containerSize.height)
    },
    onFocusChange = { focused ->
        controlCenterState.onFocusChange(focused)
        onFocusChange(focused)
    }
) {
    val cscope = rememberControlCenterScope(backgroundColor, api)
    val pagesFiltered = remember(pages) {
        derivedStateOf {
            pages.filter { p -> p.condition.invoke(cscope) }
        }
    }
    SlidingMenu(
        modifier = Modifier.fillMaxHeight(),
        orientation = Horizontal,
        state = controlCenterState,
    ) { isVisible ->
        if (!isVisible) {
            Divider(
                modifier = Modifier.fillMaxHeight().width(controlCenterDividerWidth),
                color = Color.Transparent,
                thickness = controlCenterDividerWidth
            )
        } else {
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
                            itemsIndexed(pagesFiltered.value) { idx, page ->
                                val isSelected = (pagerState.value == idx)
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
                                                pagerState.value = idx
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
                                end = controlCenterIconSize.width + 16.dp
                            ).fillMaxHeight().wrapContentSize(),
                            color = Color.Red,
                            alpha = 0.3f,
                            contentBackgroundColor = Color.Transparent
                        ) {
                            Row {
                                Divider(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(2.dp),
                                    color = Color.White.copy(0.1f),
                                    thickness = 2.dp
                                )
                                Box(
                                    modifier = Modifier.width(controlCenterExpandedWidth),
                                ) {
                                    with(pagesFiltered.value[pagerState.value]) {
                                        content(cscope)
                                    }
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
}

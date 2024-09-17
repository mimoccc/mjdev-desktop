package eu.mjdev.desktop.components.controlcenter

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.mouseClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage.Companion.rememberControlCenterScope
import eu.mjdev.desktop.components.sliding.SlidingMenu
import eu.mjdev.desktop.components.sliding.VisibilityState
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Modifier.rightShadow
import eu.mjdev.desktop.helpers.Animations.ControlCenterEnterAnimation
import eu.mjdev.desktop.helpers.Animations.ControlCenterExitAnimation
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.windows.ChromeWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName", "DEPRECATION")
@Composable
fun ControlCenter(
    api: DesktopProvider = LocalDesktop.current,
    shadowColor: Color = Color.Black.copy(alpha = 0.3f), // todo theme
    pagerState: MutableState<Int> = remember { mutableStateOf(0) },
    controlCenterState: VisibilityState = rememberVisibilityState(),
    scope: CoroutineScope = rememberCoroutineScope(),
    enterAnimation: EnterTransition = ControlCenterEnterAnimation,
    exitAnimation: ExitTransition = ControlCenterExitAnimation,
    onFocusChange: (Boolean) -> Unit = {},
    onContextMenuClick: () -> Unit = {}
) {
    val backgroundColor by remember { api.currentUser.theme.backgroundColorState }
    val backgroundAlpha by remember { api.currentUser.theme.controlCenterBackgroundAlphaState }
    val backgroundInvoker = remember { { backgroundColor } }
    val cscope = rememberControlCenterScope(backgroundInvoker, api)
    val controlCenterExpandedWidth by remember { api.currentUser.theme.controlCenterExpandedWidthState }
    val controlCenterDividerWidth by remember { api.currentUser.theme.controlCenterDividerWidthState }
    val controlCenterIconColor by remember { api.currentUser.theme.controlCenterIconColorState }
    val controlCenterIconSize by remember { api.currentUser.theme.controlCenterIconSizeState }
    val pages by remember { api.controlCenterPagesState }
    val pagesFiltered = remember(pages) {
        derivedStateOf {
            pages.filter { p -> p.condition.invoke(cscope) }
        }
    }
    ChromeWindow(
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
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentSize()
                        .background(backgroundColor.copy(alpha = backgroundAlpha)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight().wrapContentSize(),
                    ) {
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
                                            .mouseClickable {
                                                if (buttons.isSecondaryPressed) {
                                                    onContextMenuClick()
                                                } else {
                                                    scope.launch {
                                                        pagerState.value = idx
                                                    }
                                                }
                                            },
                                        imageVector = page.icon,
                                        contentDescription = "",
                                        tint = if (isSelected) backgroundColor else controlCenterIconColor
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .padding(
                                        end = controlCenterIconSize.width + 16.dp
                                    )
                                    .fillMaxHeight()
                                    .wrapContentSize(),
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
                                        modifier = Modifier
                                            .width(controlCenterExpandedWidth)
                                            .rightShadow(
                                                color = shadowColor,
                                                offsetX = 4.dp,
                                                blur = 10.dp,
                                            ),
                                    ) {
                                        with(pagesFiltered.value[pagerState.value]) {
                                            content(cscope)
                                        }
                                    }
                                }
                            }
                            Divider(
                                modifier = Modifier.fillMaxHeight().width(controlCenterDividerWidth),
                                color = Color.White.copy(0.1f),
                                thickness = controlCenterDividerWidth
                            )
                        }
                    }
                }
            }
        }
    }
}

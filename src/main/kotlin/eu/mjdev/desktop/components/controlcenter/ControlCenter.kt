package eu.mjdev.desktop.components.controlcenter

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage.ControlCenterPageScope
import eu.mjdev.desktop.components.sliding.SlidingMenu
import eu.mjdev.desktop.components.sliding.VisibilityState
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.helpers.animation.Animations.ControlCenterEnterAnimation
import eu.mjdev.desktop.helpers.animation.Animations.ControlCenterExitAnimation
import eu.mjdev.desktop.helpers.internal.Palette.Companion.rememberBackgroundColor
import eu.mjdev.desktop.helpers.internal.Palette.Companion.rememberBorderColor
import eu.mjdev.desktop.helpers.internal.Palette.Companion.rememberIconTintColor
import eu.mjdev.desktop.helpers.internal.Palette.Companion.rememberTextColor
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.windows.ChromeWindow
import eu.mjdev.desktop.windows.ChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName", "DEPRECATION")
@Composable
fun ControlCenter(
    api: DesktopProvider = LocalDesktop.current,
    pagerState: MutableState<Int> = rememberState(0),
    controlCenterState: VisibilityState = rememberVisibilityState(),
    scope: CoroutineScope = rememberCoroutineScope(),
    enterAnimation: EnterTransition = ControlCenterEnterAnimation,
    exitAnimation: ExitTransition = ControlCenterExitAnimation,
    onFocusChange: (Boolean) -> Unit = {},
    onContextMenuClick: () -> Unit = {}
) {
    val backgroundColor by rememberBackgroundColor(api)
    val backgroundAlpha by remember { api.currentUser.theme.controlCenterBackgroundAlphaState }
    val backgroundInvoker = remember { { backgroundColor } }
    val textColor by rememberTextColor(api)
    val borderColor by rememberBorderColor(api)
    val iconsTintColor by rememberIconTintColor(api)
    val controlCenterExpandedWidth by remember { api.currentUser.theme.controlCenterExpandedWidthState }
    val controlCenterDividerWidth by remember { api.currentUser.theme.controlCenterDividerWidthState }
    val controlCenterIconSize by remember { api.currentUser.theme.controlCenterIconSizeState }
    val pages by remember { api.controlCenterPagesState }
    val pagesFiltered = rememberCalculated(pages) { pages.filter { p -> p.condition.invoke(api) } }
    val position by rememberCalculated { WindowPosition.Aligned(Alignment.TopEnd) }
    val size by rememberCalculated {
        if (controlCenterState.isVisible) {
            DpSize(controlCenterExpandedWidth, api.containerSize.height)
        } else {
            DpSize(controlCenterDividerWidth, api.containerSize.height)
        }
    }
    val windowState: ChromeWindowState = rememberChromeWindowState(position = position, size = size)
    ChromeWindow(
        visible = true,
        windowState = windowState,
        enterAnimation = enterAnimation,
        exitAnimation = exitAnimation,
        onFocusChange = { focused ->
            controlCenterState.onFocusChange(focused)
            onFocusChange(focused)
        }
    ) {
        SlidingMenu(
            modifier = Modifier.fillMaxHeight(),
            orientation = Horizontal,
            state = controlCenterState,
            onPointerEnter = {
                controlCenterState.show()
                windowState.requestFocus()
            },
            onPointerLeave = {
                // controlCenterState.hide()
            }
        ) { isVisible ->
            if (!isVisible) {
                Divider(
                    modifier = Modifier.fillMaxHeight()
                        .width(controlCenterDividerWidth),
                    color = borderColor,
                    thickness = controlCenterDividerWidth
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentSize()
                        .background(
                            backgroundColor.copy(alpha = backgroundAlpha)
                        ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
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
                                                color = if (isSelected) iconsTintColor else borderColor,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onTap = {
                                                        pagerState.value = idx
                                                    }
                                                )
                                            }
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
                                        tint = if (isSelected) backgroundColor else textColor
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .padding(
                                        end = controlCenterIconSize.width + 16.dp
                                    )
                                    .fillMaxHeight(),
                            ) {
                                Row(
                                    Modifier.fillMaxHeight()
                                ) {
                                    Divider(
                                        modifier = Modifier.fillMaxHeight()
                                            .width(2.dp),
                                        color = borderColor,
                                        thickness = 2.dp
                                    )
                                    Box(
                                        modifier = Modifier.width(controlCenterExpandedWidth),
                                    ) {
                                        with(pagesFiltered.value[pagerState.value]) {
                                            content(ControlCenterPageScope(api, backgroundInvoker, cache))
                                        }
                                    }
                                    Divider(
                                        modifier = Modifier.fillMaxHeight()
                                            .width(2.dp),
                                        color = borderColor,
                                        thickness = 2.dp
                                    )
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

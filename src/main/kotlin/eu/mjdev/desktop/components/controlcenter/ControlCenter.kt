package eu.mjdev.desktop.components.controlcenter

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.sliding.SlidingMenu
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.onLeftClick
import eu.mjdev.desktop.extensions.Compose.onMousePress
import eu.mjdev.desktop.extensions.Compose.onRigntClick
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.helpers.animation.Animations.ControlCenterEnterAnimation
import eu.mjdev.desktop.helpers.animation.Animations.ControlCenterExitAnimation
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindow
import eu.mjdev.desktop.windows.ChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("FunctionName")
@Composable
fun ControlCenter(
    pagerState: MutableState<Int> = rememberState(0),
    controlCenterState: VisibilityState = rememberVisibilityState(),
    scope: CoroutineScope = rememberCoroutineScope(),
    enterAnimation: EnterTransition = ControlCenterEnterAnimation,
    exitAnimation: ExitTransition = ControlCenterExitAnimation,
    onFocusChange: (Boolean) -> Unit = {},
    onContextMenuClick: () -> Unit = {}
) = withDesktopScope {
    // todo remove those
    val backgroundAlpha by remember { theme.controlCenterBackgroundAlphaState }
    val controlCenterExpandedWidth by remember { theme.controlCenterExpandedWidthState }
    val controlCenterDividerWidth by remember { theme.controlCenterDividerWidthState }
    val controlCenterIconSize by remember { theme.controlCenterIconSizeState }
    //
    val pagesFiltered =
        rememberCalculated(controlCenterPages) { controlCenterPages.filter { p -> p.condition.invoke(api) } }
    val position by rememberCalculated { WindowPosition.Aligned(Alignment.TopEnd) }
    val size by rememberCalculated {
        if (controlCenterState.isVisible) {
            DpSize(controlCenterExpandedWidth, containerSize.height)
        } else {
            DpSize(controlCenterDividerWidth, containerSize.height)
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
                                            .onMousePress {
                                                onLeftClick {
                                                    scope.launch {
                                                        pagerState.value = idx
                                                    }
                                                }
                                                onRigntClick {
                                                    scope.launch {
                                                        pagerState.value = idx
                                                    }
                                                    onContextMenuClick()
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
                                        pagesFiltered.value[pagerState.value].render()
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

@Preview
@Composable
fun ControlCenterPreview() = ControlCenter()

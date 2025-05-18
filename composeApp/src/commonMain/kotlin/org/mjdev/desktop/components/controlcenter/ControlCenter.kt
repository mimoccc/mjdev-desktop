package org.mjdev.desktop.components.controlcenter

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.blur.BlurPanel
import org.mjdev.desktop.components.sliding.SlidingPanel
import org.mjdev.desktop.components.sliding.base.VisibilityState
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.orElse
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync
import org.mjdev.desktop.extensions.Modifier.onLeftClick
import org.mjdev.desktop.extensions.Modifier.onMouseEnter
import org.mjdev.desktop.extensions.Modifier.onMousePress
import org.mjdev.desktop.extensions.Modifier.onRightClick
import org.mjdev.desktop.extensions.MutableStateExt.rememberCalculated
import org.mjdev.desktop.extensions.MutableStateExt.rememberComputed
import org.mjdev.desktop.extensions.MutableStateExt.rememberState
import org.mjdev.desktop.helpers.animation.Animations.ControlCenterEnterAnimation
import org.mjdev.desktop.helpers.animation.Animations.ControlCenterExitAnimation
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.extensions.Compose.preview

// todo : automatic history before close, one or more steps back, 1s switch
@Composable
fun ControlCenter(
    modifier: Modifier = Modifier,
    controlCenterState: VisibilityState = rememberVisibilityState(),
    pagerState: MutableState<Int> = rememberState(0),
    enterAnimation: EnterTransition = ControlCenterEnterAnimation,
    exitAnimation: ExitTransition = ControlCenterExitAnimation,
    onContextMenuClick: () -> Unit = {},
    onFocusChange: (focused: Boolean) -> Unit = {},
    onTooltip: (item: Any?) -> Unit = {}
) = withDesktopContext {
    val controlCenterExpandedWidth: Dp by rememberComputed(
        theme.controlCenterExpandedWidthPercent,
        containerSize.width
    ) {
        (containerSize.width / 100f) * theme.controlCenterExpandedWidthPercent.orElse { 0 }
    }
    val pagesFiltered by rememberCalculated(controlCenterPages) {
        controlCenterPages.filter { p -> p.condition(context) }
    }
    SlidingPanel(
        modifier = modifier.fillMaxHeight(),
        orientation = Horizontal,
        state = controlCenterState,
        enterAnimation = enterAnimation,
        exitAnimation = exitAnimation,
        onPointerEnter = {
            onFocusChange(true)
        }
    ) { isVisible ->
        if (!isVisible) {
            Divider(
                modifier = Modifier.fillMaxHeight()
                    .width(controlCenterDividerWidth),
                color = if (isDebug) Color.Red else Color.Transparent,
                thickness = controlCenterDividerWidth
            )
        } else {
            BlurPanel(
                modifier = Modifier.fillMaxHeight()
                    .matchParentSize()
            )
            Box(
                modifier = Modifier.fillMaxHeight()
                    .wrapContentSize()
                    .background(
                        backgroundColor.copy(alpha = controlCenterBackgroundAlpha)
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
                            itemsIndexed(pagesFiltered) { idx, page ->
                                Icon(
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .size(controlCenterIconSize)
                                        .background(
                                            color = if (pagerState.value == idx) iconsTintColor else borderColor,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = {
                                                    pagerState.value = idx
                                                }
                                            )
                                        }
                                        .onMouseEnter {
                                            onTooltip(page.name)
                                        }
                                        .onMousePress {
                                            onLeftClick {
                                                runAsync {
                                                    pagerState.value = idx
                                                }
                                            }
                                            onRightClick {
                                                runAsync {
                                                    pagerState.value = idx
                                                }
                                                onContextMenuClick()
                                            }
                                        },
                                    imageVector = page.icon,
                                    contentDescription = "",
                                    tint = if (pagerState.value == idx) backgroundColor else textColor
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
                                Column(
                                    modifier = Modifier.width(controlCenterExpandedWidth),
                                ) {
                                    val page = pagesFiltered[pagerState.value]
                                    Box(modifier = Modifier.weight(1f)) {
                                        page.Render()
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
                            modifier = Modifier.fillMaxHeight().width(2.dp),
                            color = Color.White.copy(0.1f),
                            thickness = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ControlCenterPreview() = preview {
    ControlCenter(
        controlCenterState = rememberVisibilityState(true)
    )
}
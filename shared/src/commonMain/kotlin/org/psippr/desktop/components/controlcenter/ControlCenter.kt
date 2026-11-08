package org.psippr.desktop.components.controlcenter

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.psippr.desktop.components.blur.BlurPanel
import org.psippr.desktop.components.sliding.SlidingPanel
import org.psippr.desktop.components.sliding.base.VisibilityState
import org.psippr.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Compose.orElse
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.Modifier.onMouseEnter
import org.psippr.desktop.extensions.Modifier.onMousePress
import org.psippr.desktop.extensions.MutableStateExt.rememberCalculated
import org.psippr.desktop.extensions.MutableStateExt.rememberComputed
import org.psippr.desktop.extensions.MutableStateExt.rememberState
import org.psippr.desktop.helpers.animation.Animations.ControlCenterEnterAnimation
import org.psippr.desktop.helpers.animation.Animations.ControlCenterExitAnimation

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
    onTooltip: (item: Any?) -> Unit = {},
) = withDesktopContext {
    val controlCenterExpandedWidth: Dp by rememberComputed(
        theme.controlCenterExpandedWidthPercent,
        containerSize.width,
    ) {
        (containerSize.width / 100f) * theme.controlCenterExpandedWidthPercent.orElse { 0 }
    }
    // old: conditions read once, so a tab whose adapter or device came later never showed
//    val pagesFiltered by rememberCalculated(controlCenterPages) {
//        controlCenterPages.filter { p -> p.condition(context) }
//    }
    var pagesFiltered by remember(controlCenterPages) {
        mutableStateOf(controlCenterPages.filter { p -> p.condition(context) })
    }
    // re-checked on every open, off the UI thread (a condition may ask nmcli); the open tab stays
    LaunchedEffect(controlCenterState.isVisible) {
        if (!controlCenterState.isVisible) return@LaunchedEffect
        val fresh = withContext(Dispatchers.IO) { controlCenterPages.filter { p -> p.condition(context) } }
        if (fresh != pagesFiltered) {
            pagerState.value = PageSelection.indexAfterChange(pagesFiltered, fresh, pagerState.value)
            pagesFiltered = fresh
        }
    }
    SlidingPanel(
        modifier = modifier.fillMaxHeight(),
        orientation = Horizontal,
        state = controlCenterState,
        enterAnimation = enterAnimation,
        exitAnimation = exitAnimation,
        onPointerEnter = {
            onFocusChange(true)
        },
    ) { isVisible ->
        if (!isVisible) {
            VerticalDivider(
                thickness = controlCenterDividerWidth,
                color = if (isDebug) Color.Red else Color.Transparent,
            )
        } else {
            BlurPanel(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .matchParentSize(),
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .wrapContentSize()
                        .background(
                            backgroundColor.copy(alpha = controlCenterBackgroundAlpha),
                        ),
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                ) {
                    Box(
                        contentAlignment = Alignment.TopEnd,
                    ) {
                        LazyColumn(
                            modifier =
                                Modifier
                                    .fillMaxHeight()
                                    .background(backgroundColor)
                                    .padding(8.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            itemsIndexed(pagesFiltered) { idx, page ->
                                Icon(
                                    modifier =
                                        Modifier
                                            .padding(vertical = 8.dp)
                                            .size(controlCenterIconSize)
                                            .background(
                                                color = if (pagerState.value == idx) iconsTintColor else borderColor,
                                                shape = RoundedCornerShape(8.dp),
                                            ).onMouseEnter {
                                                onTooltip(page.name)
                                            }.pointerInput(idx) {
                                                // Single canonical tap detector. A second
                                                // competing onMousePress handler here used to eat
                                                // taps, so switching a tab took many clicks.
                                                detectTapGestures(
                                                    onTap = { pagerState.value = idx },
                                                    onLongPress = {
                                                        pagerState.value = idx
                                                        onContextMenuClick()
                                                    },
                                                )
                                            },
                                    imageVector = page.icon,
                                    contentDescription = "",
                                    tint = if (pagerState.value == idx) backgroundColor else textColor,
                                )
                            }
                        }
                        Box(
                            modifier =
                                Modifier
                                    .padding(
                                        end = controlCenterIconSize.width + 16.dp,
                                    ).fillMaxHeight(),
                        ) {
                            Row(
                                Modifier.fillMaxHeight(),
                            ) {
                                VerticalDivider(
                                    thickness = 2.dp,
                                    color = borderColor,
                                )
                                Column(
                                    modifier = Modifier.width(controlCenterExpandedWidth),
                                ) {
                                    // old: only the shown page existed, every tab switch loaded from scratch
//                                    val page = pagesFiltered[pagerState.value]
//                                    Box(modifier = Modifier.weight(1f)) {
//                                        page.Render()
//                                    }
                                    // old: the pager composed and drew every page over each other
//                                    Box(modifier = Modifier.weight(1f)) {
//                                        PreloadedPages(
//                                            pages = pagesFiltered,
//                                            current = pagerState.value,
//                                            modifier = Modifier.fillMaxSize(),
//                                        )
//                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        NeighbourPages(
                                            pages = pagesFiltered,
                                            current = pagerState.value,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }
                                }
                                VerticalDivider(
                                    thickness = 2.dp,
                                    color = borderColor,
                                )
                            }
                        }
                        VerticalDivider(
                            thickness = 2.dp,
                            color = Color.White.copy(0.1f),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewControlCenter() = preview {
    ControlCenter(
        controlCenterState = rememberVisibilityState(true),
    )
}

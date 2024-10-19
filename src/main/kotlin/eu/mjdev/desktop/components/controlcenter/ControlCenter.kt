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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.blur.BlurPanel
import eu.mjdev.desktop.components.sliding.SlidingPanel
import eu.mjdev.desktop.extensions.Compose.onLeftClick
import eu.mjdev.desktop.extensions.Compose.onMousePress
import eu.mjdev.desktop.extensions.Compose.onRightClick
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.extensions.Compose.rememberComputed
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.extensions.Compose.runAsync
import eu.mjdev.desktop.helpers.animation.Animations.ControlCenterEnterAnimation
import eu.mjdev.desktop.helpers.animation.Animations.ControlCenterExitAnimation
import eu.mjdev.desktop.helpers.keyevents.GlobalKeyListener.Companion.globalKeyEventHandler
import eu.mjdev.desktop.helpers.mouseevents.GlobalMouseListener.Companion.globalMouseEventHandler
import eu.mjdev.desktop.helpers.mouseevents.MouseRange
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindow
import eu.mjdev.desktop.windows.ChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Suppress("FunctionName")
@Composable
fun ControlCenter(
    pagerState: MutableState<Int> = rememberState(0),
    enterAnimation: EnterTransition = ControlCenterEnterAnimation,
    exitAnimation: ExitTransition = ControlCenterExitAnimation,
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    onContextMenuClick: () -> Unit = {},
    controlCenterState: ChromeWindowState = rememberChromeWindowState(),
) = withDesktopScope {
    val controlCenterExpandedWidth by rememberComputed(
        theme.controlCenterExpandedWidth,
        containerSize.width
    ) {
        (containerSize.width / 100f) * theme.controlCenterExpandedWidth
    }
    val pagesFiltered by rememberCalculated(controlCenterPages) {
        controlCenterPages.filter { p -> p.condition.invoke(api) }
    }
    val size by rememberCalculated(controlCenterState.isVisible) {
        if (controlCenterState.isVisible) {
            DpSize(controlCenterExpandedWidth, containerSize.height)
        } else {
            DpSize(controlCenterDividerWidth, containerSize.height)
        }
    }
    val position by rememberComputed(size) {
        WindowPosition.Absolute(
            containerSize.width - size.width,
            0.dp
        )
    }
    val mouseRange by rememberCalculated(
        containerSize,
        size
    ) {
        MouseRange(
            x = containerSize.width - size.width,
            y = 0.dp,
            width = size.width,
            height = containerSize.height
        )
    }
    globalKeyEventHandler(
        isEnabled = { controlCenterState.isVisible && controlCenterState.enabled }
    ) {
        onEscape {
            controlCenterState.hide()
            true
        }
    }
    globalMouseEventHandler {
        onPointerEnter(mouseRange) {
            runAsync {
                println("Pointer enter control center.")
                controlCenterState.showOrFocus()
            }
        }
    }
    ChromeWindow(
        name = "ControlCenter",
        visible = true,
        windowState = controlCenterState,
        enterAnimation = enterAnimation,
        exitAnimation = exitAnimation,
        onFocusChange = onFocusChange,
        position = position,
        size = size,
        onCreated = {
            controlCenterState.size = size
            controlCenterState.position = position
        }
    ) {
        SlidingPanel(
            modifier = Modifier.fillMaxHeight(),
            orientation = Horizontal,
            state = controlCenterState,
            onPointerEnter = {
                runAsync {
                    controlCenterState.show()
                    controlCenterState.requestFocus()
                }
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
                                            page.render()
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
    LaunchedEffect(position, size) {
        controlCenterState.size = size
    }
}

// todo
@Preview
@Composable
fun ControlCenterPreview() = preview {
    ControlCenter()
}

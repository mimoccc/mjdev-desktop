package org.mjdev.desktop.components.controlcenter

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.MutableStateExt.rememberCalculated
import org.mjdev.desktop.extensions.MutableStateExt.rememberComputed
import org.mjdev.desktop.extensions.MutableStateExt.rememberState
import org.mjdev.desktop.helpers.animation.Animations.ControlCenterEnterAnimation
import org.mjdev.desktop.helpers.animation.Animations.ControlCenterExitAnimation
import org.mjdev.desktop.helpers.mouseevents.MouseRange
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.orElse
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync
import org.mjdev.desktop.windows.ChromeWindow
import org.mjdev.desktop.windows.ChromeWindowState
import org.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Suppress("FunctionName")
@Composable
fun ControlCenterWindow(
    pagerState: MutableState<Int> = rememberState(0),
    enterAnimation: EnterTransition = ControlCenterEnterAnimation,
    exitAnimation: ExitTransition = ControlCenterExitAnimation,
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    onContextMenuClick: () -> Unit = {},
    controlCenterState: ChromeWindowState = rememberChromeWindowState(),
    onTooltip: (item: Any?) -> Unit = {},
) = withDesktopContext {
    val controlCenterExpandedWidth: Dp by rememberComputed(
        theme.controlCenterExpandedWidthPercent,
        containerSize.width
    ) {
        (containerSize.width / 100f) * theme.controlCenterExpandedWidthPercent.orElse { 0 }
    }
    val size by rememberCalculated(controlCenterState.isVisible) {
        if (controlCenterState.isVisible) {
            DpSize(controlCenterExpandedWidth, containerSize.height)
        } else {
            DpSize(controlCenterDividerWidth, containerSize.height)
        }
    }
    val position by rememberComputed(
        size,
        controlCenterState.isVisible
    ) {
        DpOffset(
            containerSize.width - size.width,
            0.dp
        )
    }
    val mouseRange by rememberCalculated(
        containerSize,
        size
    ) {
        MouseRange(
            x = containerSize.width - controlCenterDividerWidth,
            y = 0.dp,
            width = size.width,
            height = containerSize.height
        )
    }
    ChromeWindow(
        name = "ControlCenter",
        visible = true,
        windowState = controlCenterState,
        onFocusChange = onFocusChange,
        position = position,
        size = size,
        onCreated = {
            controlCenterState.size = size
            controlCenterState.position = position
        },
        isGlobalKeyHandlerEnabled = {
            controlCenterState.isVisible && controlCenterState.enabled
        },
        onGlobalKey = {
            onEscape {
                runAsync {
                    controlCenterState.hide()
                }
                true
            }
            onEscape {
                runAsync {
                    controlCenterState.hide()
                }
                true
            }
        },
        isGlobalMouseHandlerEnabled = { isUserLoggedIn.orElse { false } },
        onGlobalMouse = {
            onPointerEnter(mouseRange) {
                runAsync {
                    println("Pointer enter control center.")
                    controlCenterState.showOrFocus()
                }
            }
        }
    ) {
        ControlCenter(
            controlCenterState = controlCenterState,
            pagerState = pagerState,
            enterAnimation = enterAnimation,
            exitAnimation = exitAnimation,
            onContextMenuClick = onContextMenuClick,
            onTooltip = onTooltip,
            onFocusChange = { focused ->
                if (focused) {
                    runAsync {
                        controlCenterState.showOrFocus()
                    }
                }
            }
        )
        LaunchedEffect(position, size) {
            controlCenterState.size = size
        }
    }
}

// todo
@Suppress("unused")
@Preview
@Composable
fun ControlCenterPreview() = preview {
    ControlCenterWindow()
}

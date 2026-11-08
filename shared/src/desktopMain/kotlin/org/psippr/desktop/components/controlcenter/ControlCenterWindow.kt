package org.psippr.desktop.components.controlcenter

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Compose.orElse
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.MutableStateExt.rememberCalculated
import org.psippr.desktop.extensions.MutableStateExt.rememberComputed
import org.psippr.desktop.extensions.MutableStateExt.rememberState
import org.psippr.desktop.helpers.animation.Animations.ControlCenterEnterAnimation
import org.psippr.desktop.helpers.animation.Animations.ControlCenterExitAnimation
import org.psippr.desktop.helpers.mouseevents.MouseRange
import org.psippr.desktop.windows.ChromeWindow
import org.psippr.desktop.windows.ChromeWindowState
import org.psippr.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

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
        containerSize.width,
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
        controlCenterState.isVisible,
    ) {
        DpOffset(
            containerSize.width - size.width,
            0.dp,
        )
    }
    ChromeWindow(
        name = "ControlCenter",
        visible = true,
        blur = true,
        // Not alwaysOnTop: DesktopState's focusOnShow (see MainWindow's register call) already
        // raises + focuses this window whenever the reveal hotspot is entered — a normal
        // toFront(), not a permanent OS-level pin that would keep it above external apps too.
        windowState = controlCenterState,
        onFocusChange = onFocusChange,
        position = position,
        size = size,
        // old: separate setters; the size setter moves by the size difference from a stored
        // position that moveBy never updates, so an expand could shift the panel twice (x = 652)
//        onCreated = {
//            controlCenterState.size = size
//            controlCenterState.position = position
//        },
        // position and size together, absolute, like the dock: nothing relative to race
        onCreated = {
            runAsync { controlCenterState.applyBounds(position, size) }
        },
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
            },
        )
        // old: only the size, moved relative to the stored position (see onCreated)
//        LaunchedEffect(position, size) {
//            controlCenterState.size = size
//        }
        LaunchedEffect(position, size) {
            controlCenterState.applyBounds(position, size)
        }
    }
}

// todo
@Suppress("unused")
@Preview
@Composable
fun PreviewControlCenterWindow() = preview {
    ControlCenterWindow()
}

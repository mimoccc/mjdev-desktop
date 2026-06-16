package org.mjdev.desktop.components.aidesktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.MutableStateExt.rememberCalculated
import org.mjdev.desktop.helpers.mouseevents.MouseRange
import org.mjdev.desktop.windows.ChromeWindow
import org.mjdev.desktop.windows.ChromeWindowState
import org.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Composable
fun AiDesktopDockWindow(
    panelState: ChromeWindowState = rememberChromeWindowState(visible = true),
    menuVisible: Boolean,
    onMenuVisibleChange: (Boolean) -> Unit,
    onControlCenterVisibleChange: (Boolean) -> Unit,
) = withDesktopContext {
    val dockHeight = AiDesktopMetrics.DockHeight + AiDesktopMetrics.DesktopPadding.times(2)
    var dockOverlapped by remember { mutableStateOf(false) }
    var dockRevealedByPointer by remember { mutableStateOf(false) }
    val size by rememberCalculated(
        panelState.isVisible,
        containerSize.width,
    ) {
        DpSize(
            width = containerSize.width,
            height = if (panelState.isVisible) dockHeight else AiDesktopMetrics.EdgeHotspotWidth,
        )
    }
    val position by rememberCalculated(
        containerSize,
        size,
    ) {
        DpOffset(
            x = containerSize.width,
            y = containerSize.height,
        )
    }
    val revealRange by rememberCalculated(containerSize) {
        MouseRange(
            x = 0.dp,
            y = containerSize.height - AiDesktopMetrics.EdgeHotspotWidth,
            width = containerSize.width,
            height = AiDesktopMetrics.EdgeHotspotWidth,
        )
    }
    val leaveRange by rememberCalculated(containerSize) {
        MouseRange(
            x = 0.dp,
            y = containerSize.height - dockHeight,
            width = containerSize.width,
            height = dockHeight,
        )
    }
    ChromeWindow(
        name = "DockBar",
        visible = true,
        alwaysOnTop = true,
        windowState = panelState,
        position = position,
        size = size,
        onCreated = {
            panelState.position = position
            panelState.size = size
        },
        isGlobalMouseHandlerEnabled = { isUserLoggedIn },
        onGlobalMouse = {
            onPointerEnter(revealRange) {
                runAsync {
                    dockRevealedByPointer = true
                    panelState.showOrFocus()
                }
            }
            onPointerLeave(leaveRange) {
                runAsync {
                    dockRevealedByPointer = false
                    if (dockOverlapped && !menuVisible && panelState.isVisible) {
                        panelState.hide()
                    } else if (!dockOverlapped && panelState.isNotVisible) {
                        panelState.show()
                    }
                }
            }
        },
        isGlobalKeyHandlerEnabled = { panelState.isVisible || menuVisible },
        onGlobalKey = {
            onEscape {
                runAsync {
                    onMenuVisibleChange(false)
                    panelState.hide()
                }
                true
            }
            onMenuKey {
                runAsync {
                    panelState.showOrFocus()
                    onControlCenterVisibleChange(false)
                    onMenuVisibleChange(!menuVisible)
                }
                true
            }
        },
    ) {
        if (panelState.isVisible) {
            Box(modifier = Modifier.fillMaxSize()) {
                AiDesktopBottomDock(
                    favoriteApps = favoriteApps,
                    onMenuClick = {
                        onControlCenterVisibleChange(false)
                        onMenuVisibleChange(!menuVisible)
                    },
                    onAppLaunched = {
                        onMenuVisibleChange(false)
                    },
                )
            }
        }
    }
    LaunchedEffect(size, position) {
        panelState.position = position
        panelState.size = size
    }
    LaunchedEffect(containerSize, menuVisible, dockRevealedByPointer) {
        while (true) {
            dockOverlapped =
                AiDesktopCompositorWindowReader
                    .readWindows()
                    .any { window ->
                        window.overlapsBottomStrip(
                            screenWidth = containerSize.width,
                            screenHeight = containerSize.height,
                            stripHeight = dockHeight,
                        )
                    }
            if (dockOverlapped && !menuVisible && !dockRevealedByPointer && panelState.isVisible) {
                panelState.hide()
            } else if ((!dockOverlapped || menuVisible || dockRevealedByPointer) && panelState.isNotVisible) {
                panelState.show()
            }
            delay(AiDesktopTiming.DockOverlapPollMillis)
        }
    }
}

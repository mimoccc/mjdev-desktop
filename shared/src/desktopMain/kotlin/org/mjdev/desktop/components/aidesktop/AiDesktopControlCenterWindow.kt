package org.mjdev.desktop.components.aidesktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.MutableStateExt.rememberCalculated
import org.mjdev.desktop.helpers.mouseevents.MouseRange
import org.mjdev.desktop.windows.ChromeWindow
import org.mjdev.desktop.windows.ChromeWindowState
import org.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Composable
fun AiDesktopControlCenterWindow(
    controlCenterState: ChromeWindowState = rememberChromeWindowState(),
    selectedTab: AiControlCenterTab,
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    onTabSelected: (AiControlCenterTab) -> Unit,
) = withDesktopContext {
    val size by rememberCalculated(
        visible,
        containerSize,
    ) {
        DpSize(
            width = if (visible) AiDesktopMetrics.ControlCenterWidth else AiDesktopMetrics.EdgeHotspotWidth,
            height = containerSize.height,
        )
    }
    val position by rememberCalculated(
        containerSize,
    ) {
        DpOffset(
            x = containerSize.width,
            y = containerSize.height,
        )
    }
    val edgeRange by rememberCalculated(containerSize) {
        MouseRange(
            x = containerSize.width - AiDesktopMetrics.EdgeHotspotWidth,
            y = 0.dp,
            width = AiDesktopMetrics.EdgeHotspotWidth,
            height = containerSize.height,
        )
    }
    ChromeWindow(
        name = "ControlCenter",
        visible = true,
        alwaysOnTop = true,
        windowState = controlCenterState,
        position = position,
        size = size,
        onCreated = {
            controlCenterState.position = position
            controlCenterState.size = size
        },
        onFocusChange = { focused ->
            if (!focused && visible) {
                onVisibleChange(false)
            }
        },
        isGlobalMouseHandlerEnabled = { isUserLoggedIn },
        onGlobalMouse = {
            onPointerEnter(edgeRange) {
                runAsync {
                    onVisibleChange(true)
                    controlCenterState.showOrFocus()
                }
            }
        },
        isGlobalKeyHandlerEnabled = { visible },
        onGlobalKey = {
            onEscape {
                runAsync {
                    onVisibleChange(false)
                    controlCenterState.hide()
                }
                true
            }
        },
    ) {
        if (visible) {
            Box(modifier = Modifier.fillMaxSize()) {
                AiDesktopControlCenterPanel(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                )
            }
        }
    }
    LaunchedEffect(visible, size, position) {
        controlCenterState.position = position
        controlCenterState.size = size
        if (visible) {
            controlCenterState.show()
        } else {
            controlCenterState.hide()
        }
    }
}

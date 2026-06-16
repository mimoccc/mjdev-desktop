package org.mjdev.desktop.components.aidesktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.desktop.Desktop
import org.mjdev.desktop.components.tooltip.TooltipState
import org.mjdev.desktop.components.tooltip.rememberTooltipState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Modifier.onMouseEnter

@Composable
fun AiDesktopShell(
    tooltipState: TooltipState = rememberTooltipState(),
    onTooltip: (item: Any?) -> Unit = {},
    menuVisible: Boolean? = null,
    menuMode: AiDesktopMenuMode? = null,
    controlCenterVisible: Boolean? = null,
    selectedControlTab: AiControlCenterTab? = null,
    showBottomDock: Boolean = true,
    showControlCenterPanel: Boolean = true,
    showControlCenterHotspot: Boolean = true,
    onMenuVisibleChange: ((Boolean) -> Unit)? = null,
    onMenuModeChange: ((AiDesktopMenuMode) -> Unit)? = null,
    onControlCenterVisibleChange: ((Boolean) -> Unit)? = null,
    onSelectedControlTabChange: ((AiControlCenterTab) -> Unit)? = null,
) = withDesktopContext {
    var localMenuVisible by remember { mutableStateOf(false) }
    var localMenuMode by remember { mutableStateOf(AiDesktopMenuMode.Window) }
    var localControlCenterVisible by remember { mutableStateOf(false) }
    var localSelectedControlTab by remember { mutableStateOf(AiControlCenterTab.System) }
    val isMenuVisible = menuVisible ?: localMenuVisible
    val currentMenuMode = menuMode ?: localMenuMode
    val isControlCenterVisible = controlCenterVisible ?: localControlCenterVisible
    val currentSelectedControlTab = selectedControlTab ?: localSelectedControlTab
    val setMenuVisible: (Boolean) -> Unit = { visible ->
        localMenuVisible = visible
        onMenuVisibleChange?.invoke(visible)
    }
    val setMenuMode: (AiDesktopMenuMode) -> Unit = { mode ->
        localMenuMode = mode
        onMenuModeChange?.invoke(mode)
    }
    val setControlCenterVisible: (Boolean) -> Unit = { visible ->
        localControlCenterVisible = visible
        onControlCenterVisibleChange?.invoke(visible)
    }
    val setSelectedControlTab: (AiControlCenterTab) -> Unit = { tab ->
        localSelectedControlTab = tab
        onSelectedControlTabChange?.invoke(tab)
    }
    Desktop(
        tooltipState = tooltipState,
        padding = PaddingValues(),
        onTooltip = onTooltip,
        onLeftMouseClick = {
            setMenuVisible(false)
            setControlCenterVisible(false)
        },
        widgets = {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                AiDesktopWidgetLayer()
                if (showControlCenterHotspot) {
                    Box(
                        modifier =
                            androidx.compose.ui.Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(AiDesktopMetrics.EdgeHotspotWidth)
                                .background(Color.Transparent)
                                .onMouseEnter {
                                    setControlCenterVisible(true)
                                    setMenuVisible(false)
                                },
                    )
                }
                if (isMenuVisible) {
                    AiDesktopMenuPanel(
                        mode = currentMenuMode,
                        categories = appCategories,
                        apps = allApps,
                        onModeChange = setMenuMode,
                        onClose = { setMenuVisible(false) },
                    )
                }
                if (showControlCenterPanel && isControlCenterVisible) {
                    AiDesktopControlCenterPanel(
                        selectedTab = currentSelectedControlTab,
                        onTabSelected = setSelectedControlTab,
                    )
                }
                AiDesktopRightDock(
                    onControlCenterClick = {
                        setControlCenterVisible(!isControlCenterVisible)
                        setMenuVisible(false)
                    },
                )
                if (showBottomDock) {
                    AiDesktopBottomDock(
                        favoriteApps = favoriteApps,
                        onMenuClick = {
                            setMenuVisible(!isMenuVisible)
                            setControlCenterVisible(false)
                        },
                        onAppLaunched = {
                            setMenuVisible(false)
                        },
                    )
                }
            }
        },
    )
}

@Preview
@Composable
fun PreviewAiDesktopShell() =
    preview {
        AiDesktopShell()
    }

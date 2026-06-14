package org.mjdev.desktop.components.desktoppanel

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.context.DesktopContextScope
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync
import org.mjdev.desktop.extensions.MutableStateExt.rememberCalculated
import org.mjdev.desktop.extensions.MutableStateExt.rememberComputed
import org.mjdev.desktop.extensions.PaddingValues.height
import org.mjdev.desktop.helpers.mouseevents.MouseRange
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.windows.ChromeWindow
import org.mjdev.desktop.windows.ChromeWindowState
import org.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Suppress("FunctionName")
@Composable
fun DesktopPanelWindow(
    iconSize: DpSize = DpSize(48.dp, 48.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    showMenuIcon: Boolean = true,
    panelState: ChromeWindowState = rememberChromeWindowState(),
    menuState: ChromeWindowState = rememberChromeWindowState(),
    onMenuIconClicked: () -> Unit = {
        runAsync {
            menuState.showOrFocus()
        }
    },
    onMenuIconContextMenuClicked: () -> Unit = {},
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    onAppClick: DesktopContextScope.(IApp) -> Unit = { app ->
        runAsync {
            app.start()
        }
    },
    onAppContextMenuClick: (IApp) -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onTooltip: (item: Any?) -> Unit = {},
) = withDesktopContext {
    val panelHeight: (visible: Boolean) -> Dp = { visible ->
        if (visible) {
            iconSize.height +
                iconPadding.height.times(2) +
                iconOuterPadding.height.times(2) +
                theme.panelContentPadding.times(2)
        } else {
            panelDividerWidth
        }
    }
    val size by rememberComputed(
        panelState.isVisible,
        panelState.enabled,
        containerSize.width,
        containerSize.height,
    ) {
        DpSize(
            containerSize.width,
            panelHeight(
                if (panelState.enabled) {
                    panelState.isVisible
                } else {
                    true
                },
            ),
        )
    }
    val position by rememberComputed(size) {
        DpOffset(
            9.dp,
            containerSize.height,
        )
    }
    val mouseRange by rememberCalculated(
        containerSize,
        size,
        position,
    ) {
        MouseRange(
            x = 0.dp,
            y = containerSize.height - controlCenterDividerWidth,
            width = containerSize.width,
            height = size.height,
        )
    }
    ChromeWindow(
        name = "DesktopPanel",
        visible = true,
        position = position,
        size = size,
        onFocusChange = onFocusChange,
        windowState = panelState,
        onCreated = {
            panelState.position = position
            panelState.size = size
        },
        isGlobalKeyHandlerEnabled = {
            panelState.isVisible && panelState.enabled
        },
        onGlobalKey = {
            onEscape {
                runAsync {
                    menuState.hide()
                    panelState.hide()
                }
                true
            }
            onMenuKey {
                runAsync {
                    panelState.showOrFocus()
                    menuState.showOrFocus()
                }
                true
            }
        },
        isGlobalMouseHandlerEnabled = {
            isUserLoggedIn && panelState.enabled
        },
        onGlobalMouse = {
            onPointerEnter(mouseRange) {
                runAsync {
//                    println("Pointer enter desktop panel.")
                    if (menuState.isNotVisible) {
                        panelState.showOrFocus()
                    }
                }
            }
        },
    ) {
        DesktopPanel(
            iconSize = iconSize,
            iconPadding = iconPadding,
            iconOuterPadding = iconOuterPadding,
            showMenuIcon = showMenuIcon,
            panelState = panelState,
            onMenuIconClicked = {
                onMenuIconClicked()
            },
            onMenuIconContextMenuClicked = onMenuIconContextMenuClicked,
            onAppClick = onAppClick,
            onAppContextMenuClick = onAppContextMenuClick,
            onLanguageClick = onLanguageClick,
            onTooltip = onTooltip,
            onFocusChange = { focused ->
                if (focused) {
                    runAsync {
                        panelState.show()
                        if (menuState.isVisible) {
                            menuState.focus()
                        }
                    }
                }
            },
        )
    }
    LaunchedEffect(size, position) {
        panelState.size = size
    }
}

// todo
@Preview
@Composable
fun PreviewDesktopPanelWindow() =
    preview {
        DesktopPanelWindow()
    }

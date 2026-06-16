package org.mjdev.desktop.components.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.mjdev.desktop.components.aidesktop.AiControlCenterTab
import org.mjdev.desktop.components.aidesktop.AiDesktopControlCenterWindow
import org.mjdev.desktop.components.aidesktop.AiDesktopDockWindow
import org.mjdev.desktop.components.aidesktop.AiDesktopMenuMode
import org.mjdev.desktop.components.aidesktop.AiDesktopShell
import org.mjdev.desktop.components.greeter.GreeterWindow
import org.mjdev.desktop.components.info.InfoWindow
import org.mjdev.desktop.components.installer.InstallerWindow
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.components.tooltip.TooltipState
import org.mjdev.desktop.components.tooltip.rememberTooltipState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.helpers.system.shell.Shell
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.windows.DesktopWindow

// todo focus manager
@Composable
fun MainWindow() =
    withDesktopContext {
        val tooltipState: TooltipState = rememberTooltipState()
        var menuVisible by remember { mutableStateOf(false) }
        var menuMode by remember { mutableStateOf(AiDesktopMenuMode.Window) }
        var controlCenterVisible by remember { mutableStateOf(false) }
        var selectedControlTab by remember { mutableStateOf(AiControlCenterTab.System) }
        val installWindowState = rememberVisibilityState()
        val infoWindowState = rememberVisibilityState(false) // (api.isFirstStart || api.isDebug) // todo
        val onTooltip: (item: Any?) -> Unit = { item ->
//        println("Tooltip: $item")
            tooltipState.show(item)
        }
        DesktopWindow {
            AiDesktopShell(
                tooltipState = tooltipState,
                onTooltip = onTooltip,
                menuVisible = menuVisible,
                menuMode = menuMode,
                controlCenterVisible = controlCenterVisible,
                selectedControlTab = selectedControlTab,
                showBottomDock = false,
                showControlCenterPanel = false,
                showControlCenterHotspot = false,
                onMenuVisibleChange = { visible -> menuVisible = visible },
                onMenuModeChange = { mode -> menuMode = mode },
                onControlCenterVisibleChange = { visible -> controlCenterVisible = visible },
                onSelectedControlTabChange = { tab -> selectedControlTab = tab },
            )
        }
        AiDesktopDockWindow(
            menuVisible = menuVisible,
            onMenuVisibleChange = { visible -> menuVisible = visible },
            onControlCenterVisibleChange = { visible -> controlCenterVisible = visible },
        )
        AiDesktopControlCenterWindow(
            selectedTab = selectedControlTab,
            visible = controlCenterVisible,
            onVisibleChange = { visible ->
                controlCenterVisible = visible
                if (visible) {
                    menuVisible = false
                }
            },
            onTabSelected = { tab -> selectedControlTab = tab },
        )
        GreeterWindow()
        InfoWindow(
            visibleState = infoWindowState,
            showInstallWindow = {
                runAsync {
                    infoWindowState.hide()
                    installWindowState.show()
                }
            },
        )
        InstallerWindow(
            visibleState = installWindowState,
        )
        DisposableEffect(Unit) {
            Log.i("App started with args: $appArgs")
            Log.i("First start : $isFirstStart")
            Log.i("Debug mode : $isDebug")
            Shell {
                if (!isDebug) {
                    Log.i("Starting autostart apps")
//                autoStartApps()
                } else {
                    Log.i("Starting autostart apps omitted in debug mode.")
                }
            }
            onDispose {
                dispose()
                Log.i("App ended.")
            }
        }
    }

// todo
@Suppress("unused")
@Preview
@Composable
fun PreviewMainWindow() =
    preview {
        MainWindow()
    }

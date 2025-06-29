package org.mjdev.desktop.components.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.mjdev.desktop.components.appsmenu.AppsMenuState.Companion.rememberAppsMenuState
import org.mjdev.desktop.components.appsmenu.AppsMenuWindow
import org.mjdev.desktop.components.controlcenter.ControlCenterWindow
import org.mjdev.desktop.components.desktop.Desktop
import org.mjdev.desktop.components.desktop.widgets.MemoryChart
import org.mjdev.desktop.components.desktoppanel.DesktopPanelWindow
import org.mjdev.desktop.components.greeter.GreeterWindow
import org.mjdev.desktop.components.info.InfoWindow
import org.mjdev.desktop.components.installer.InstallerWindow
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.components.tooltip.TooltipState
import org.mjdev.desktop.components.tooltip.rememberTooltipState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.isDesign
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.MutableStateExt.rememberCalculated
import org.mjdev.desktop.helpers.system.shell.Shell
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import org.mjdev.desktop.windows.DesktopWindow

// todo focus manager
@Composable
fun MainWindow() = withDesktopContext {
    val tooltipState: TooltipState = rememberTooltipState()
    val controlCenterState = rememberChromeWindowState(
        visible = isDesign
    )
    val panelState = rememberChromeWindowState(
        hideDelay = panelHideDelay,
        visible = isDesign || !panelAutoHideEnabled,
        enabled = panelAutoHideEnabled,
    )
    val menuState = rememberChromeWindowState(
        visible = isDesign
    )
    val appsMenuState = rememberAppsMenuState(
        visible = isDesign
    )
    val installWindowState = rememberVisibilityState()
    val infoWindowState = rememberVisibilityState(false) //(api.isFirstStart || api.isDebug) // todo
    val bottomPadding by rememberCalculated(
        panelState.enabled,
        panelState.height
    ) {
        if (panelState.enabled) 0.dp
        else max(0.dp, panelState.height)
    }
    val onTooltip: (item: Any?) -> Unit = { item ->
//        println("Tooltip: $item")
        tooltipState.show(item)
    }
    DesktopWindow(
        panelState = panelState,
        controlCenterState = controlCenterState,
        menuState = menuState,
    ) {
        Desktop(
            tooltipState = tooltipState,
            onTooltip = onTooltip,
            padding = PaddingValues(
                bottom = bottomPadding
            ),
            widgets = {
                MemoryChart(
                    modifier = Modifier.size(350.dp, 300.dp)
                        .align(Alignment.BottomEnd)
                )
//                WebView(
//                    modifier = Modifier
//                        .size(800.dp, 600.dp)
//                        .align(Alignment.Center),
//                    url = "https://www.google.com"
//                )
            },
            onLeftMouseClick = {
                runAsync {
                    panelState.hide()
                    menuState.hide()
                    controlCenterState.hide()
                }
            },
            onRightMouseClick = {
//                contextMenuState.show()
            }
        )
    }
    DesktopPanelWindow(
        onTooltip = onTooltip,
        panelState = panelState,
        menuState = menuState,
        onFocusChange = { focused ->
//            Log.d("panel focus : $focused")
            val menuIsVisible = appsMenuState.isVisible || menuState.isVisible
            if (panelState.enabled) {
                if (!menuIsVisible && !focused) {
                    runAsync {
                        panelState.hide()
                    }
                }
            }
        }
    )
    AppsMenuWindow(
        menuState = menuState,
        panelState = panelState,
        appsMenuState = appsMenuState,
        onTooltip = onTooltip,
        onFocusChange = { focused ->
            Log.d("menu focus : $focused")
//            if (!focused) {
//                menuState.hide()
//            }
        }
    )
    ControlCenterWindow(
        onTooltip = onTooltip,
        controlCenterState = controlCenterState,
        onFocusChange = { focused ->
//            Log.d("control center focus : $focused")
            if (!focused) {
                runAsync {
                    controlCenterState.hide()
                }
            }
        }
    )
    GreeterWindow()
    InfoWindow(
        visibleState = infoWindowState,
        showInstallWindow = {
            runAsync {
                infoWindowState.hide()
                installWindowState.show()
            }
        }
    )
    InstallerWindow(
        visibleState = installWindowState
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
    LaunchedEffect(menuState.isVisible) {
        appsMenuState.isVisible = menuState.isVisible
    }
}

// todo
@Suppress("unused")
@Preview
@Composable
fun PreviewMainWindow() = preview {
    MainWindow()
}

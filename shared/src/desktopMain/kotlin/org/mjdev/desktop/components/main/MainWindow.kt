package org.mjdev.desktop.components.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.mjdev.desktop.components.appsmenu.AppsMenuState.Companion.rememberAppsMenuState
import org.mjdev.desktop.components.appsmenu.AppsMenuWindow
import org.mjdev.desktop.components.controlcenter.ControlCenterWindow
import org.mjdev.desktop.components.desktop.Desktop
import org.mjdev.desktop.components.desktop.widgets.MemoryChart
import org.mjdev.desktop.components.desktoppanel.DesktopPanelWindow
import org.mjdev.desktop.components.dockbar.DockBarWindow
import org.mjdev.desktop.components.greeter.GreeterWindow
import org.mjdev.desktop.components.info.InfoWindow
import org.mjdev.desktop.components.installer.InstallerWindow
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.components.tooltip.TooltipState
import org.mjdev.desktop.components.tooltip.TooltipWindow
import org.mjdev.desktop.components.tooltip.rememberTooltipState
import org.mjdev.desktop.state.DesktopState
import org.mjdev.desktop.state.DesktopStateDriver
import org.mjdev.desktop.data.PanelLocation
import org.mjdev.desktop.state.SurfaceKind
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
    val controlCenterState =
        rememberChromeWindowState(
            visible = isDesign,
        )
    // The bar is visible by default now; DesktopState hides it only when a window overlaps it or
    // the control center opens (see DesktopPolicy). No per-window autohide timer anymore.
    val panelState =
        rememberChromeWindowState(
            visible = true,
        )
    val desktopState = remember { DesktopState(scope) }
    val menuState =
        rememberChromeWindowState(
            visible = isDesign,
        )
    val appsMenuState =
        rememberAppsMenuState(
            visible = isDesign,
        )
    val installWindowState = rememberVisibilityState()
    val infoWindowState = rememberVisibilityState(false) // (api.isFirstStart || api.isDebug) // todo
    val bottomPadding by rememberCalculated(
        panelState.enabled,
        panelState.height,
    ) {
        if (panelState.enabled) {
            0.dp
        } else {
            max(0.dp, panelState.height)
        }
    }
    val onTooltip: (item: Any?) -> Unit = { item ->
//        println("Tooltip: $item")
        tooltipState.show(item)
    }
    // Register the three surfaces with the central DesktopState. It owns all show/hide decisions
    // (bar/menu/control-center coordination) via DesktopPolicy; the windows below just render.
    DisposableEffect(desktopState, containerSize) {
        val cw = containerSize.width.value.toDouble()
        val ch = containerSize.height.value.toDouble()
        val edge = controlCenterDividerWidth.value.toDouble()
        fun rectOf(state: org.mjdev.desktop.windows.ChromeWindowState) =
            DesktopState.ScreenRect(
                left = state.position.x.value.toDouble(),
                top = state.position.y.value.toDouble(),
                right = (state.position.x + state.size.width).value.toDouble(),
                bottom = (state.position.y + state.size.height).value.toDouble(),
            )
        desktopState.register(
            kind = SurfaceKind.Bar,
            window = panelState,
            bounds = { rectOf(panelState) },
            // reveal strip along whichever edge the dock lives on (only used when overlapped).
            // Derived from PanelLocation so drag-to-edge later needs no change here.
            revealHotspot = {
                when (panelLocation) {
                    PanelLocation.Bottom -> DesktopState.ScreenRect(0.0, ch - edge, cw, ch)
                    PanelLocation.Top -> DesktopState.ScreenRect(0.0, 0.0, cw, edge)
                    PanelLocation.Left -> DesktopState.ScreenRect(0.0, 0.0, edge, ch)
                    PanelLocation.Right -> DesktopState.ScreenRect(cw - edge, 0.0, cw, ch)
                }
            },
        )
        desktopState.register(
            kind = SurfaceKind.Menu,
            window = menuState,
            bounds = { rectOf(menuState) },
            focusOnShow = true,
            onApply = { visible -> appsMenuState.isVisible = visible },
        )
        desktopState.register(
            kind = SurfaceKind.ControlCenter,
            window = controlCenterState,
            bounds = { rectOf(controlCenterState) },
            // right reveal strip — hovering it opens the control center
            revealHotspot = { DesktopState.ScreenRect(cw - edge, 0.0, cw, ch) },
            focusOnShow = true,
        )
        onDispose { }
    }
    // One place feeds global pointer + clicks + Escape into DesktopState.
    DesktopStateDriver(desktopState)
    DesktopWindow(
        panelState = panelState,
        controlCenterState = controlCenterState,
        menuState = menuState,
    ) {
        Desktop(
            onTooltip = onTooltip,
            padding =
                PaddingValues(
                    bottom = bottomPadding,
                ),
            widgets = {
                MemoryChart(
                    modifier =
                        Modifier
                            .size(350.dp, 300.dp)
                            .align(Alignment.BottomEnd),
                )
//                WebView(
//                    modifier = Modifier
//                        .size(800.dp, 600.dp)
//                        .align(Alignment.Center),
//                    url = "https://www.google.com"
//                )
            },
            onLeftMouseClick = {
                // desktop click = click outside every surface -> dismiss menu + control center
                desktopState.dismissTransients()
            },
            onRightMouseClick = {
//                contextMenuState.show()
            },
        )
    }
//        DesktopPanelWindow(
//            onTooltip = onTooltip,
//            panelState = panelState,
//            menuState = menuState,
//            onFocusChange = { focused ->
// //            Log.d("panel focus : $focused")
//                val menuIsVisible = appsMenuState.isVisible || menuState.isVisible
//                if (panelState.enabled) {
//                    if (!menuIsVisible && !focused) {
//                        runAsync {
//                            panelState.hide()
//                        }
//                    }
//                }
//            },
//        )
    DockBarWindow(
        onTooltip = onTooltip,
        panelState = panelState,
        menuState = menuState,
        controlCenterState = controlCenterState,
        // The menu button toggles the apps menu through DesktopState (which also closes the
        // control center). All show/hide policy lives there now, not in the window.
        onMenuIconClicked = { desktopState.toggleMenu() },
        onFocusChange = {},
    )
    AppsMenuWindow(
        menuState = menuState,
        panelState = panelState,
        appsMenuState = appsMenuState,
        onTooltip = onTooltip,
        // Route menu-close (app launched, action clicked) through DesktopState so its intent stays
        // in sync — a direct menuState.hide() would desync and the menu could pop back.
        onCloseMenu = { desktopState.closeMenu() },
        onFocusChange = {},
    )
    ControlCenterWindow(
        onTooltip = onTooltip,
        controlCenterState = controlCenterState,
        // Dismissal (click-outside / Escape) is owned by DesktopState now, so the window no longer
        // hides itself on focus loss (that fought focus-follows-mouse).
        onFocusChange = {},
    )
    // Standalone auto-hide tooltip window — every onTooltip above lands here.
    TooltipWindow(
        tooltipState = tooltipState,
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
        // Headless/kiosk convenience: start the remote-desktop (VNC) server at boot when
        // MJDEV_VNC_AUTOSTART is set, so a machine with no local input can be driven remotely.
        if (System.getenv("MJDEV_VNC_AUTOSTART")?.toBooleanStrictOrNull() == true) {
            runAsync { context.remoteDesktop.start() }
        }
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
fun PreviewMainWindow() = preview {
    MainWindow()
}

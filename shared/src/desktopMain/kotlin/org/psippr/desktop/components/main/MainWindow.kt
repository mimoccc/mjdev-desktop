package org.psippr.desktop.components.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.psippr.desktop.components.appsmenu.AppsMenuState.Companion.rememberAppsMenuState
import org.psippr.desktop.components.appsmenu.AppsMenuWindow
import org.psippr.desktop.components.controlcenter.ControlCenterWindow
import org.psippr.desktop.components.desktop.Desktop
import org.psippr.desktop.components.desktop.widgets.PluginWidgets
import org.psippr.desktop.components.desktoppanel.DesktopPanelWindow
import org.psippr.desktop.components.dockbar.DockBarWindow
import org.psippr.desktop.components.greeter.GreeterWindow
import org.psippr.desktop.components.info.InfoWindow
import org.psippr.desktop.components.installer.InstallerWindow
import org.psippr.desktop.components.notifications.NotificationWindow
import org.psippr.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.psippr.desktop.components.tooltip.TooltipState
import org.psippr.desktop.components.tooltip.TooltipWindow
import org.psippr.desktop.components.tooltip.rememberTooltipState
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.data.PanelLocation
import org.psippr.desktop.extensions.Compose.isDesign
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.MutableStateExt.rememberCalculated
import org.psippr.desktop.helpers.system.shell.Shell
import org.psippr.desktop.log.Log
import org.psippr.desktop.managers.notifications.NotificationService
import org.psippr.desktop.state.DesktopState
import org.psippr.desktop.state.DesktopStateDriver
import org.psippr.desktop.state.SurfaceKind
import org.psippr.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import org.psippr.desktop.windows.DesktopWindow

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
    // UI-thread scope: DesktopState shows/hides/focuses AWT windows, which must run on the EDT.
    // context.scope is Dispatchers.Default (background) — using it made windows "show" in state
    // but never actually raise/render.
    val uiScope = rememberCoroutineScope()
    val desktopState = remember { DesktopState(uiScope) }
    // height of the shown dock, reported by DockBarWindow; app windows covering it hide the dock
    var dockShownHeight by remember { mutableStateOf(0.dp) }
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

        fun rectOf(state: org.psippr.desktop.windows.ChromeWindowState) = DesktopState.ScreenRect(
            left =
                state.position.x.value
                    .toDouble(),
            top =
                state.position.y.value
                    .toDouble(),
            right = (state.position.x + state.size.width).value.toDouble(),
            bottom = (state.position.y + state.size.height).value.toDouble(),
        )
        desktopState.register(
            kind = SurfaceKind.Bar,
            window = panelState,
            bounds = { rectOf(panelState) },
            // raise (toFront, no keyboard focus steal) on reveal so it visibly comes back above
            // whatever app window was overlapping it, without a permanent alwaysOnTop pin that
            // would keep it above external apps even when it shouldn't be.
            raiseOnShow = true,
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
            // the shown dock along its edge: an app window covering it hides the dock (reveal on
            // hover), so the dock no longer sits over the bottom of app windows
            overlapStrip = {
                val dock = dockShownHeight.value.toDouble()
                when (panelLocation) {
                    PanelLocation.Bottom -> DesktopState.ScreenRect(0.0, ch - dock, cw, ch)
                    PanelLocation.Top -> DesktopState.ScreenRect(0.0, 0.0, cw, dock)
                    PanelLocation.Left -> DesktopState.ScreenRect(0.0, 0.0, dock, ch)
                    PanelLocation.Right -> DesktopState.ScreenRect(cw - dock, 0.0, cw, ch)
                }.takeIf { dock > 0.0 }
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
    // The apps-menu CONTENT (SlidingPanel) is gated on appsMenuState.isVisible; mirror it to the
    // menu window's visibility on the Compose (Main) thread so the content always renders when the
    // window opens. (onApply also sets it, but this guarantees it on the UI thread.)
    LaunchedEffect(menuState.isVisible) {
        appsMenuState.isVisible = menuState.isVisible
    }
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
                // Widgets are plugins now (the memory chart is the built-in one), switched on and
                // off in the control center Plugins tab. Previous direct usage kept for history:
                // MemoryChart(
                //     modifier = Modifier.size(350.dp, 300.dp).align(Alignment.BottomEnd),
                // )
                PluginWidgets()
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
        onShownHeight = { height -> dockShownHeight = height },
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
    // Popup of the newest notification (auto-hiding); the history is on the control center home tab.
    NotificationWindow(controlCenterState = controlCenterState)
    // while a notification popup is shown, the bar and the control center step aside (back on hover)
    val notificationPopup = context.notificationsManager.popup
    LaunchedEffect(notificationPopup?.id) {
        desktopState.setNotificationShown(notificationPopup != null)
    }
    // the standard Linux notification service, so notify-send and libnotify apps show here too
    DisposableEffect(Unit) {
        val service = NotificationService(context.notificationsManager, context.scope)
        context.scope.launch(Dispatchers.IO) { service.start() }
        onDispose { service.stop() }
    }
    // old: in-desktop login overlay; its login was a stub (no PAM) and it stuck on top of every app
    // whenever the user lookup failed. The desktop runs only after the greetd / PAM login, so the
    // login screen is the greeter (LoginWindow) alone.
//    GreeterWindow()
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
        // PSIPPR_VNC_AUTOSTART is set, so a machine with no local input can be driven remotely.
        if (System.getenv("PSIPPR_VNC_AUTOSTART")?.toBooleanStrictOrNull() == true) {
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

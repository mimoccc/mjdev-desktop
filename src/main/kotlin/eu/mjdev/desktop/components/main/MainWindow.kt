package eu.mjdev.desktop.components.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.appsmenu.AppsMenu
import eu.mjdev.desktop.components.controlcenter.ControlCenter
import eu.mjdev.desktop.components.desktop.Desktop
import eu.mjdev.desktop.components.desktoppanel.DesktopPanel
import eu.mjdev.desktop.components.greeter.Greeter
import eu.mjdev.desktop.components.info.InfoWindow
import eu.mjdev.desktop.components.installer.Installer
import eu.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import eu.mjdev.desktop.windows.DesktopWindow

// todo focus manager
@Composable
fun MainWindow() = withDesktopScope {
    val controlCenterState = rememberChromeWindowState()
    val panelState = rememberChromeWindowState(visible = true, enabled = panelAutoHideEnabled)
    val menuState = rememberChromeWindowState()
    val installWindowSate = rememberVisibilityState()
    val infoWindowSate = rememberVisibilityState(false) //(api.isFirstStart || api.isDebug) // todo
    DesktopWindow(
        panelState = panelState,
        controlCenterState = controlCenterState,
        menuState = menuState
    ) {
        Desktop(
            modifier = Modifier.fillMaxSize(),
            panelState = panelState,
        )
        DesktopPanel(
            panelState = panelState,
            menuState = menuState,
            onMenuIconClicked = { if (!menuState.isVisible) menuState.show() },
            onFocusChange = { focus ->
                if (panelState.enabled && !focus && !menuState.isVisible) panelState.hide()
            }
        )
        AppsMenu(
            menuState = menuState,
            panelState = panelState,
            onFocusChange = { focus ->
                if (!focus) {
                    menuState.hide()
                }
            }
        )
        ControlCenter(
            controlCenterState = controlCenterState,
            onFocusChange = { focus ->
                if (!focus) controlCenterState.hide()
            }
        )
        Greeter()
        InfoWindow(
            state = infoWindowSate,
            showInstallWindow = {
                infoWindowSate.hide()
                installWindowSate.show()
            }
        )
        Installer(
            state = installWindowSate
        )
    }
    DisposableEffect(Unit) {
        println("App started with args: $appArgs")
        println("First start : $isFirstStart")
        println("Debug mode : $isDebug")
        Shell {
            if (!isDebug) {
                println("Starting autostart apps")
                autoStartApps()
            } else {
                println("Starting autostart apps omitted in debug mode.")
            }
        }
        onDispose {
            dispose()
            println("App ended.")
        }
    }
}

// todo
@Preview
@Composable
fun MainWindowPreview() = preview {
    MainWindow()
}

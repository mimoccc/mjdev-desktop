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
import eu.mjdev.desktop.log.Log
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import eu.mjdev.desktop.windows.DesktopWindow

// todo focus manager
@Composable
fun MainWindow() = withDesktopScope {
    val controlCenterState = rememberChromeWindowState(
//        hideDelay = controlPanelHideDelay
    )
    val panelState = rememberChromeWindowState(
        visible = true,
        enabled = panelAutoHideEnabled,
        hideDelay = panelHideDelay
    )
    val menuState = rememberChromeWindowState()
    val installWindowState = rememberVisibilityState()
    val infoWindowState = rememberVisibilityState(false) //(api.isFirstStart || api.isDebug) // todo
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
            onMenuIconClicked = { menuState.toggle() },
            onFocusChange = { focused ->
                if (panelState.enabled && menuState.isNotVisible && !focused) {
                    panelState.hide()
                }
            }
        )
        AppsMenu(
            menuState = menuState,
            panelState = panelState,
            onFocusChange = { focused ->
                if (!focused) {
                    menuState.hide()
                }
            }
        )
        ControlCenter(
            controlCenterState = controlCenterState,
            onFocusChange = { focused ->
                if (!focused) {
                    controlCenterState.hide()
                }
            }
        )
        Greeter()
        InfoWindow(
            state = infoWindowState,
            showInstallWindow = {
                infoWindowState.hide()
                installWindowState.show()
            }
        )
        Installer(
            state = installWindowState
        )
    }
    DisposableEffect(Unit) {
        Log.i("App started with args: $appArgs")
        Log.i("First start : $isFirstStart")
        Log.i("Debug mode : $isDebug")
        Shell {
            if (!isDebug) {
                Log.i("Starting autostart apps")
                autoStartApps()
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
@Preview
@Composable
fun MainWindowPreview() = preview {
    MainWindow()
}

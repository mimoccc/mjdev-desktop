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
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.DesktopWindow

@Composable
fun MainWindow() = withDesktopScope {
    val controlCenterState = rememberVisibilityState()
    val panelState = rememberVisibilityState(true, panelAutoHideEnabled)
    val menuState = rememberVisibilityState()
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
                if (!focus) menuState.hide()
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
        onDispose {
            api.close()
        }
    }
}

@Preview
@Composable
fun MainWindowPreview() = MainWindow()

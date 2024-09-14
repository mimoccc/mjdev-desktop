package eu.mjdev.desktop

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.application
import eu.mjdev.desktop.components.appsmenu.AppsMenu
import eu.mjdev.desktop.components.controlcenter.ControlCenter
import eu.mjdev.desktop.components.desktoppanel.DesktopPanel
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.windows.MainWindow

fun main() = application(
    exitProcessOnExit = true
) {
    val scope = rememberCoroutineScope()
    val controlCenterState = rememberVisibilityState()
    val panelState = rememberVisibilityState()
    val menuState = rememberVisibilityState()

    MaterialTheme {
        CompositionLocalProvider(
            LocalDesktop provides DesktopProvider(scope)
        ) {
            MainWindow(
                panelState = panelState,
                controlCenterState = controlCenterState,
                menuState = menuState
            ) {
                DesktopPanel(
                    menuState = menuState,
                    panelState = panelState,
                    onMenuIconClicked = {
                        menuState.toggle()
                    }
                )
                AppsMenu(
                    menuState = menuState,
                    panelState = panelState
                )
                ControlCenter(
                    controlCenterState = controlCenterState
                )
            }
        }
    }
}

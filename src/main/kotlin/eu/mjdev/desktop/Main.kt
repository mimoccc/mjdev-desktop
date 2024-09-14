package eu.mjdev.desktop

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.application
import eu.mjdev.desktop.components.appsmenu.AppsMenu
import eu.mjdev.desktop.components.controlcenter.ControlCenter
import eu.mjdev.desktop.components.desktoppanel.DesktopPanel
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.asyncImageLoader
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

    val handleMenuFocus: (Boolean) -> Unit = {
        if (!menuState.isWindowFocus) {
            menuState.hide()
        }
    }

    val handlePanelFocus: (Boolean) -> Unit = {

    }

    val handleControlCenterFocus: (Boolean) -> Unit = {
        if (!controlCenterState.isWindowFocus) {
            controlCenterState.hide()
        }
    }

    MaterialTheme {
        CompositionLocalProvider(
            LocalDesktop provides DesktopProvider(
                scope = scope,
                imageLoader = asyncImageLoader()
            ),
        ) {
            MainWindow(
                panelState = panelState,
                controlCenterState = controlCenterState,
                menuState = menuState
            ) {
                DesktopPanel(
                    panelState = panelState,
                    onMenuIconClicked = { if (!menuState.isVisible) menuState.show() },
                    onFocusChange = { focus -> handlePanelFocus(focus) }
                )
                AppsMenu(
                    menuState = menuState,
                    panelState = panelState,
                    onFocusChange = { focus -> handleMenuFocus(focus) }
                )
                ControlCenter(
                    controlCenterState = controlCenterState,
                    onFocusChange = { focus -> handleControlCenterFocus(focus) }
                )
            }
        }
    }
}

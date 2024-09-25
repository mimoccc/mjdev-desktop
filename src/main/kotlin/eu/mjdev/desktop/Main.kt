package eu.mjdev.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.application
import eu.mjdev.desktop.components.appsmenu.AppsMenu
import eu.mjdev.desktop.components.controlcenter.ControlCenter
import eu.mjdev.desktop.components.desktop.Desktop
import eu.mjdev.desktop.components.desktoppanel.DesktopPanel
import eu.mjdev.desktop.components.greeter.Greeter
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.DesktopProvider.Companion.rememberDesktopProvider
import eu.mjdev.desktop.windows.MainWindow

fun main(
    @Suppress("unused")
    args: Array<String>
) = application(
    exitProcessOnExit = true
) {
    val api = rememberDesktopProvider()
    val controlCenterState = rememberVisibilityState()
    val panelState = rememberVisibilityState()
    val menuState = rememberVisibilityState()
    val handleMenuFocus: (Boolean) -> Unit = { focus ->
        if (!menuState.isWindowFocus && !focus && !controlCenterState.isWindowFocus) {
            menuState.hide()
        }
    }
    val handlePanelFocus: (Boolean) -> Unit = { focus ->
        if (!focus) {
            if (!menuState.isVisible) {
                panelState.hide()
            }
        }
    }
    val handleControlCenterFocus: (Boolean) -> Unit = { focus ->
        if (!focus) {
            controlCenterState.hide()
        }
    }
    MaterialTheme {
        CompositionLocalProvider(LocalDesktop provides api) {
            MainWindow(
                panelState = panelState,
                controlCenterState = controlCenterState,
                menuState = menuState
            ) {
                Desktop(
                    modifier = Modifier.fillMaxSize()
                )
                DesktopPanel(
                    panelState = panelState,
                    menuState = menuState,
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
                Greeter()
//                InfoWindow()
            }
            DisposableEffect(Unit) {
                onDispose {
                    api.dispose()
                }
            }
        }
    }
}

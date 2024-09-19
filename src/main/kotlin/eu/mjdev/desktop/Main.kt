package eu.mjdev.desktop

import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import eu.mjdev.desktop.components.appsmenu.AppsMenu
import eu.mjdev.desktop.components.charts.MemoryChart
import eu.mjdev.desktop.components.controlcenter.ControlCenter
import eu.mjdev.desktop.components.desktoppanel.DesktopPanel
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.DesktopProvider.Companion.rememberDesktopProvider
import eu.mjdev.desktop.windows.MainWindow

@Suppress("unused")
fun main(
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
        CompositionLocalProvider(
            LocalDesktop provides api
        ) {
            MainWindow(
                panelState = panelState, controlCenterState = controlCenterState, menuState = menuState
            ) {
//                ComposeWebView(
//                    modifier = Modifier.size(640.dp, 480.dp).align(Alignment.Center)
//                )
                MemoryChart(
                    modifier = Modifier.size(350.dp, 300.dp).align(Alignment.BottomEnd)
                )
                DesktopPanel(panelState = panelState,
                    menuState = menuState,
                    onMenuIconClicked = { menuState.toggle() },
                    onFocusChange = { focus -> handlePanelFocus(focus) })
                AppsMenu(menuState = menuState,
                    panelState = panelState,
                    onFocusChange = { focus -> handleMenuFocus(focus) })
                ControlCenter(controlCenterState = controlCenterState,
                    onFocusChange = { focus -> handleControlCenterFocus(focus) })
            }
            DisposableEffect(Unit) {
                onDispose {
                    api.dispose()
                }
            }
        }
    }
}

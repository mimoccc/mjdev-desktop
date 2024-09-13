package eu.mjdev.desktop

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.application
import eu.mjdev.desktop.components.appsmenu.AppsMenu
import eu.mjdev.desktop.components.desktoppanel.DesktopPanel
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.windows.MainWindow

fun main() = application(
    exitProcessOnExit = true
) {
    val scope = rememberCoroutineScope()
    val controlCenterState = rememberVisibilityState(enabled = false, startState = true)
    val panelState = rememberVisibilityState(enabled = false, startState = true)
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
                    modifier = Modifier.align(Alignment.BottomCenter),
                    panelState = panelState,
                    onMenuIconClicked = {
                        menuState.toggle()
                    }
                )
                AppsMenu(
                    menuState = menuState,
                    panelState = panelState
                )
//                ControlCenter(
//                    modifier = Modifier.align(Alignment.TopEnd),
//                    controlCenterState = controlCenterState
//                )
            }
        }
    }
}

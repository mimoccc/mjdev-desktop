package eu.mjdev.desktop

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import eu.mjdev.desktop.components.appsmenu.AppsMenu
import eu.mjdev.desktop.components.controlcenter.ControlCenter
import eu.mjdev.desktop.components.desktoppanel.DesktopPanel
import eu.mjdev.desktop.components.slidemenu.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.windows.MainWindow

fun main() = application(
    exitProcessOnExit = true
) {
    val scope = rememberCoroutineScope()
    CompositionLocalProvider(
        LocalDesktop provides DesktopProvider(scope)
    ) {
        MaterialTheme {
            val controlCenterState = rememberVisibilityState()
            val panelState = rememberVisibilityState()
            val menuState = rememberVisibilityState()
//            scope.launch {
//                api.askGemini("How are you today?").let {
//                    log { "Gemini answer: $it" }
//                }
//            }
            MainWindow(
                panelState = panelState,
                controlCenterState = controlCenterState,
                menuState = menuState
            ) {
//                Dialog(
//                    onDismissRequest = {},
//                    properties = DialogProperties()
//                ) {
//                    Text("dialog content")
//                }
            }
            DesktopPanel(
                panelState = panelState,
                onMenuIconClicked = {
                    menuState.toggle()
                }
            )
            AppsMenu(
                // todo
                bottomY = 64.dp,
                menuState = menuState,
                panelState = panelState
            )
            ControlCenter(
                controlCenterState = controlCenterState
            )
        }
    }
}

package eu.mjdev.desktop

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.*
import eu.mjdev.desktop.extensions.Compose.fullScreenWindow
import eu.mjdev.desktop.helpers.WindowFocusState.Companion.rememberWindowFocusState
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.LocalDesktop
import eu.mjdev.desktop.provider.data.User
import eu.mjdev.desktop.screens.MainScreen

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application(
    exitProcessOnExit = false
) {
    fullScreenWindow {
        val containerSize = LocalWindowInfo.current.containerSize
        val currentUser = User.Empty // todo get user
        val windowFocusState = rememberWindowFocusState(window)

        CompositionLocalProvider(
            LocalDesktop provides DesktopProvider(
                containerSize = containerSize,
                currentUser = currentUser,
                windowFocusState = windowFocusState
            )
        ) {
            MainScreen()
        }
    }
}

package org.mjdev.desktop

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.mjdev.desktop.context.DesktopContext.Companion.rememberDesktopContext
import org.mjdev.desktop.context.LocalDesktopContext
import org.mjdev.desktop.main.MainView

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(
        document.body!!
    ) {
        CompositionLocalProvider(
            LocalDesktopContext provides rememberDesktopContext()
        ) {
            MainView()
        }
    }
}

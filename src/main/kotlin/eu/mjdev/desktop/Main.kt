package eu.mjdev.desktop

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.application
import eu.mjdev.desktop.components.main.MainWindow
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.DesktopProvider.Companion.rememberDesktopProvider

fun main(
    args: Array<String>
) = application(
    exitProcessOnExit = true
) {
    val api = rememberDesktopProvider()
    println("Started with args: ${args.toList()}")
    MaterialTheme {
        CompositionLocalProvider(LocalDesktop provides api) {
            MainWindow()
        }
    }
}

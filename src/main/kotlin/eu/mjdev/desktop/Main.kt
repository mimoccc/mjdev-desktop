package eu.mjdev.desktop

import androidx.compose.ui.window.application
import eu.mjdev.desktop.screens.MainWindow

fun main() = application(
    exitProcessOnExit = true
) {
    MainWindow()
}

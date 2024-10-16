package eu.mjdev.desktop

import eu.mjdev.desktop.components.main.MainWindow
import eu.mjdev.desktop.helpers.application.application

fun main(
    args: Array<String>
) = application(
    args = args.toList(),
    exitProcessOnExit = true
) {
    MainWindow()
}

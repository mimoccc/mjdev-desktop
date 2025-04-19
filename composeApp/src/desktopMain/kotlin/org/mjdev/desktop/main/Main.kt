package org.mjdev.desktop.main

import Notify
import org.mjdev.desktop.components.main.MainWindow
import org.mjdev.desktop.helpers.application.application

fun main(
    args: Array<String>
) = application(
    args = args.toList(),
    exitProcessOnExit = true
) {
    MainWindow()
    Notify(
        message= "Hello World!",
        duration=NotificationDuration.LONG
    )
}

package org.mjdev.desktop

import Notify
import org.mjdev.desktop.components.main.MainWindow

fun main(
    args: Array<String>
) = org.mjdev.desktop.helpers.application.application(
    args = args.toList(),
    exitProcessOnExit = true
) {
    MainWindow()
    Notify(
        message= "Hello World!",
        duration=NotificationDuration.LONG
    )
}

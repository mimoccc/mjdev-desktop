package org.mjdev.desktop.main

import org.mjdev.desktop.components.main.MainWindow
import org.mjdev.desktop.helpers.application.application
import org.mjdev.desktop.log.Log

fun main(args: Array<String>) =
    application(
        args = args.toList(),
        exitProcessOnExit = true,
        onStart = {
            Log.init()
        },
        onExit = {
        },
    ) {
        MainWindow()
//    Notify(
//        message = "Hello World!",
//        duration = NotificationDuration.LONG
//    )
    }

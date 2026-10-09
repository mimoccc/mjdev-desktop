package org.psippr.desktop.main

import org.psippr.desktop.components.main.MainWindow
import org.psippr.desktop.helpers.application.application
import org.psippr.desktop.helpers.font.guard.FontGuard
import org.psippr.desktop.helpers.migration.LegacyDataMigration
import org.psippr.desktop.log.Log

fun main(args: Array<String>) {
    // very first: carry user data over from the old mjdev name before anything reads or writes it
    LegacyDataMigration.migrate()
    // then, before Compose touches fonts: fonts without a family name freeze Skia at start
    FontGuard.protect(args.toList())
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
}

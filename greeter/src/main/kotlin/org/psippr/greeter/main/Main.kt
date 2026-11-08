package org.psippr.greeter.main

import org.psippr.desktop.components.greeter.LoginWindow
import org.psippr.desktop.helpers.application.application
import org.psippr.desktop.helpers.font.guard.FontGuard
import org.psippr.desktop.log.Log

/**
 * psippr greeter: greetd runs it (through psippr-greeter + psipprc) as the login screen. It shows
 * only the login window; after a good password the process exits and greetd starts the session.
 */
fun main(args: Array<String>) {
    // before Compose touches fonts: fonts without a family name freeze Skia at start
    FontGuard.protect(args.toList())
    application(
        args = args.toList(),
        exitProcessOnExit = true,
        onStart = {
            Log.init()
        },
    ) {
        LoginWindow(onLoggedIn = ::exitApplication)
    }
}

package org.mjdev.desktop.app

import android.app.Application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.mjdev.desktop.helpers.crash.CrashHandler
import org.mjdev.desktop.log.Log

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.register(this, 200L)
        Log.init()
    }
}
package org.psippr.desktop.app

import android.app.Application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.psippr.desktop.helpers.crash.CrashHandler
import org.psippr.desktop.log.Log

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.register(this, 200L)
        Log.init()
    }
}

package org.mjdev.desktop.app

import android.app.Application
import org.mjdev.desktop.helpers.crash.CrashHandler

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.register(this, 200L)
    }
}
package org.mjdev.desktop.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import android.os.PowerManager.WakeLock

class WakeLockHelper(
    val context: Context
) {
    private var wakeLock: WakeLock? = null
    private val powerManager
        get() = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    @SuppressLint("WakelockTimeout")
    fun acquireWakeLock() {
        wakeLock = powerManager?.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            "MyApp:KeepScreenOnTag"
        )
        if (wakeLock?.isHeld != true) {
            wakeLock?.acquire()
        }
    }

    fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }
}

package org.mjdev.desktop.helpers.crash

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.system.exitProcess

@Suppress("UNREACHABLE_CODE")
class CrashHandler(
    private val context: Context,
    private val restartMillis:Long = 1000
) : Thread.UncaughtExceptionHandler {
    private val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val alarmMgr
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Suppress("KotlinUnreachableCode")
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        Log.e(TAG, "Fatal: ", ex)
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            )
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmMgr.set(
                AlarmManager.RTC,
                System.currentTimeMillis() + restartMillis,
                pendingIntent
            )
        } else {
            Log.e(TAG, "No intent to restart app.")
        }
        exitProcess(10)
        defaultExceptionHandler?.uncaughtException(thread, ex)
    }

    companion object {
        private val TAG = CrashHandler::class.simpleName

        fun register(
            context: Context,
            restartMillis:Long = 1000L
        ) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context, restartMillis))
            Log.d(TAG, "Crash handler registered.")
        }
    }
}
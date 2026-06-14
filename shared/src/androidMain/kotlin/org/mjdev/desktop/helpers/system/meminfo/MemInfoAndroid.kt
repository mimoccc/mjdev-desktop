package org.mjdev.desktop.helpers.system.meminfo

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import org.mjdev.desktop.context.DesktopContext
import org.mjdev.desktop.context.IDesktopContext

class MemInfoAndroid(
    context: IDesktopContext,
) : MemInfoStub(context) {
    val aContext: Context?
        get() = (context as? DesktopContext)?.context
    val activityManager: ActivityManager? by lazy {
        aContext?.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }
    private val meminfo: ActivityManager.MemoryInfo
        get() =
            ActivityManager.MemoryInfo().apply {
                activityManager?.getMemoryInfo(this)
            }

    override val free: Double = meminfo.availMem.toDouble()

    override val total: Double = meminfo.totalMem.toDouble()
}

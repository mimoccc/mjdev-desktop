package org.mjdev.desktop.managers.connectivity

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.mjdev.desktop.context.IDesktopContext

@Suppress("FunctionName")
fun ConnectivityManager(context: IDesktopContext) =
    when (hostOs) {
        // todo other platforms
        OS.Linux -> ConnectivityManagerLinux(context)
        else -> ConnectivityManagerStub(context)
    }

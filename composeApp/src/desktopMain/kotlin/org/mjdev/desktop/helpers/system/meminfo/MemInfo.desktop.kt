package org.mjdev.desktop.helpers.system.meminfo

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.mjdev.desktop.context.IDesktopContext

actual fun MemInfo(
    context: IDesktopContext
): MemInfoStub = when (hostOs) {
    // todo other platforms
    OS.Linux -> MemInfoLinux(context)
    else -> MemInfoStub(context)
}
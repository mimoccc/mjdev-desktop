package org.mjdev.desktop.helpers.system.meminfo

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

actual fun MemInfo(): MemInfoStub = when (hostOs) {
    // todo other platforms
    OS.Linux -> MemInfoLinux()
    else -> MemInfoStub()
}
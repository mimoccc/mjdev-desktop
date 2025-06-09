package org.mjdev.desktop.helpers.system.meminfo

import org.mjdev.desktop.context.IDesktopContext

open class MemInfoStub(
    val context: IDesktopContext
) {
    open val free: Double = 100.0
    open val total: Double = 100.0

    val used: Double
        get() = total - free
}
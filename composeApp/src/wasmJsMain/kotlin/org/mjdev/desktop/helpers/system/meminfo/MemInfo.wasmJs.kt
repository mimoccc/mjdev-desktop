package org.mjdev.desktop.helpers.system.meminfo

import org.mjdev.desktop.interfaces.IDesktopContext

@Suppress("FunctionName")
actual fun MemInfo(
    context: IDesktopContext
): MemInfoStub = MemInfoStub(context)
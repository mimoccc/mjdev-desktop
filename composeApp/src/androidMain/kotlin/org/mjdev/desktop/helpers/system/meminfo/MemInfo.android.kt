package org.mjdev.desktop.helpers.system.meminfo

import org.mjdev.desktop.interfaces.IDesktopContext

actual fun MemInfo(context: IDesktopContext): MemInfoStub  = MemInfoAndroid(context)
package org.psippr.desktop.helpers.system.meminfo

import org.psippr.desktop.context.IDesktopContext

actual fun MemInfo(context: IDesktopContext): MemInfoStub = MemInfoAndroid(context)

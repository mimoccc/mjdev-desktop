/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.managers.os

import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.managers.os.base.OSManagerStub

@Suppress("FunctionName")
fun OsManager(context: IDesktopContext) = // when (hostOs) {
// todo other platforms
// OS.Linux -> OSManagerLinux(context)
    // else ->
    OSManagerStub(context)
// }

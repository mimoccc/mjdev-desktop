/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.os

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.os.base.OSManagerStub
import org.mjdev.desktop.managers.os.linux.OSManagerLinux

@Suppress("FunctionName")
fun OsManager(context: IDesktopContext) =
    when (hostOs) {
        // todo other platforms
        OS.Linux -> OSManagerLinux(context)
        else -> OSManagerStub(context)
    }

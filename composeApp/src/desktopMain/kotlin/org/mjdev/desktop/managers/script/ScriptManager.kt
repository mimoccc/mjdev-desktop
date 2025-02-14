/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.script

import org.mjdev.desktop.managers.script.base.ScriptManagerStub
import org.mjdev.desktop.managers.script.linux.ScriptManagerLinux
import org.mjdev.desktop.context.DesktopContext
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

// todo other platforms
@Suppress("unused")
fun scriptManager(
    api: DesktopContext
) = when (hostOs) {
    OS.Linux -> ScriptManagerLinux(api)
    else -> ScriptManagerStub(api)
}

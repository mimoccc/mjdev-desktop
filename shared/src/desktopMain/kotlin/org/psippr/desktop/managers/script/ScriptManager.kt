/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.managers.script

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.psippr.desktop.context.DesktopContext
import org.psippr.desktop.managers.script.base.ScriptManagerStub
import org.psippr.desktop.managers.script.linux.ScriptManagerLinux

// todo other platforms
@Suppress("unused")
fun scriptManager(api: DesktopContext) = when (hostOs) {
    OS.Linux -> ScriptManagerLinux(api)
    else -> ScriptManagerStub(api)
}

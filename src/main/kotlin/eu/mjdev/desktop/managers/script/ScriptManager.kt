/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.script

import eu.mjdev.desktop.managers.script.base.ScriptManagerStub
import eu.mjdev.desktop.managers.script.linux.ScriptManagerLinux
import eu.mjdev.desktop.provider.DesktopProvider
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

// todo other platforms
@Suppress("unused")
fun scriptManager(
    api: DesktopProvider
) = when (hostOs) {
    OS.Linux -> ScriptManagerLinux(api)
    else -> ScriptManagerStub(api)
}

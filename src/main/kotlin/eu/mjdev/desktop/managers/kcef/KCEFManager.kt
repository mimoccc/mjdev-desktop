/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.kcef

import eu.mjdev.desktop.managers.kcef.base.KCEFManagerStub
import eu.mjdev.desktop.managers.kcef.linux.KCEFManagerLinux
import eu.mjdev.desktop.provider.DesktopProvider
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

fun kcefManager(
    api: DesktopProvider
) = when (hostOs) {
    // todo other platforms
    OS.Linux -> KCEFManagerLinux(api)
    else -> KCEFManagerStub(api)
}

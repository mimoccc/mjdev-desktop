/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.os

import eu.mjdev.desktop.managers.os.base.OSManagerStub
import eu.mjdev.desktop.managers.os.linux.OSManagerLinux
import eu.mjdev.desktop.provider.DesktopProvider
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

fun osManager(
    api: DesktopProvider
) = when (hostOs) {
    // todo other platforms
    OS.Linux -> OSManagerLinux(api)
    else -> OSManagerStub(api)
}
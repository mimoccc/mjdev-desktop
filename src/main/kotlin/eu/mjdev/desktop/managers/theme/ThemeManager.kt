/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.theme

import eu.mjdev.desktop.managers.theme.base.ThemeManagerStub
import eu.mjdev.desktop.managers.theme.linux.ThemeManagerLinux
import eu.mjdev.desktop.provider.DesktopProvider
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

// todo other platforms
fun themeManager(
    api: DesktopProvider
) = when (hostOs) {
    OS.Linux -> ThemeManagerLinux(api)
    else -> ThemeManagerStub(api)
}

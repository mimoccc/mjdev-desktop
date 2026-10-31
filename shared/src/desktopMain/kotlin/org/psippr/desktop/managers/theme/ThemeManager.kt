/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.managers.theme

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.helpers.compositor.NestedRun
import org.psippr.desktop.managers.theme.base.ThemeManagerStub
import org.psippr.desktop.managers.theme.linux.ThemeManagerLinux

// old: a nested test run themed the host desktop too (gsettings, ~/.config/gtk-*/gtk.css,
// ~/.themes) and deleted its gtk.css files on exit
// // todo other platforms
// @Suppress("FunctionName")
// fun ThemeManager(context: IDesktopContext) = when (hostOs) {
//     OS.Linux -> ThemeManagerLinux(context)
//     else -> ThemeManagerStub(context)
// }

// todo other platforms

/**
 * Theme manager of the platform. A nested run (a test window on another desktop) shares the
 * user's settings with that desktop, so it gets the no-op stub and never changes them.
 */
@Suppress("FunctionName")
fun ThemeManager(context: IDesktopContext) = when {
    // old: every nested run, also one with its own config folder
//    NestedRun.isNested() -> ThemeManagerStub(context)
    NestedRun.isNested() && !NestedRun.hasOwnConfig() -> ThemeManagerStub(context)
    hostOs == OS.Linux -> ThemeManagerLinux(context)
    else -> ThemeManagerStub(context)
}

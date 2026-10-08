/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.managers.theme

import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.managers.theme.base.ThemeManagerStub

// todo other platforms
@Suppress("FunctionName")
fun ThemeManager(context: IDesktopContext) = // when (hostOs) {
//    OS.Linux -> ThemeManagerLinux(context)
//    else ->
    ThemeManagerStub(context)
// }

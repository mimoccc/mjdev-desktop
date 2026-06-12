/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.theme

import org.mjdev.desktop.managers.theme.base.ThemeManagerStub
import org.mjdev.desktop.context.IDesktopContext

// todo other platforms
@Suppress("FunctionName")
fun ThemeManager(
    context: IDesktopContext
) = //when (hostOs) {
//    OS.Linux -> ThemeManagerLinux(context)
//    else ->
        ThemeManagerStub(context)
//}

/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.theme.base

import org.mjdev.desktop.interfaces.IDesktopContext
import org.mjdev.desktop.managers.theme.IThemeManager

@Suppress("unused")
open class ThemeManagerStub(
    val context: IDesktopContext,
) : IThemeManager {
    override fun createFromPalette() {}

    override fun clearSystemTheme() {}
}

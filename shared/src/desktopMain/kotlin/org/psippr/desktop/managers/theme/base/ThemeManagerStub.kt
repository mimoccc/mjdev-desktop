/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.managers.theme.base

import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.managers.theme.IThemeManager

@Suppress("unused")
open class ThemeManagerStub(
    val context: IDesktopContext,
) : IThemeManager {
    override fun createFromPalette() {}

    override fun clearSystemTheme() {}
}

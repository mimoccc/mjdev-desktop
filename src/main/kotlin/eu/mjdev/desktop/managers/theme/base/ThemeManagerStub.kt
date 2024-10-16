/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.theme.base

import eu.mjdev.desktop.provider.DesktopProvider

@Suppress("unused")
open class ThemeManagerStub(
    val api: DesktopProvider,
) {
    open fun createFromPalette() {
        // no op
    }

    open fun clearSystemTheme() {
        // no op
    }
}

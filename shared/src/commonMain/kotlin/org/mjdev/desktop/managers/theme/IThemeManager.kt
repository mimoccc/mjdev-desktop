package org.mjdev.desktop.managers.theme

import org.mjdev.desktop.managers.base.IDelegate

interface IThemeManager : IDelegate {
    fun createFromPalette()

    fun clearSystemTheme()

    companion object {
        val EMPTY =
            object : IThemeManager {
                override fun createFromPalette() {}

                override fun clearSystemTheme() {}
            }
    }
}

package org.mjdev.desktop.managers.apps

import org.mjdev.desktop.data.Category
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.interfaces.ILocale
import org.mjdev.desktop.managers.base.IDelegate

interface IAppsManager : IDelegate {
    val categories : List<Category>
    val allApps : List<IApp>
    val favoriteApps : List<IApp>
    val currentLocale : ILocale

    companion object {
        val EMPTY = object : IAppsManager {
            override val categories: List<Category> = emptyList()
            override val allApps: List<IApp> = emptyList()
            override val favoriteApps: List<IApp> = emptyList()
            override val currentLocale: ILocale = ILocale.DEFAULT
        }
    }
}

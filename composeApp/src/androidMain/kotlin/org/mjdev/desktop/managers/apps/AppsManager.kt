/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserHandle
import android.os.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.mjdev.desktop.context.DesktopContext
import org.mjdev.desktop.data.App
import org.mjdev.desktop.data.Category
import org.mjdev.desktop.extensions.Compose.addIfNotExists
import org.mjdev.desktop.extensions.MutableStateExt.mutableStateListFlow
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.interfaces.ILocale
import java.util.Locale

@Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName")
class AppsManager(
    private val context: IDesktopContext,
    private val scope: CoroutineScope = context.scope
) : IAppsManager {
    companion object {
        val TAG = AppsManager::class.simpleName
    }

    private val androidContext = (context as? DesktopContext)?.context

    private val userManger = androidContext?.getSystemService(Context.USER_SERVICE) as UserManager

    private val launcherApps
        get() = runCatching {
            androidContext?.getSystemService(LauncherApps::class.java)
        }.getOrNull()

    val userHandles: List<UserHandle>?
        get() = runCatching {
            userManger.userProfiles
        }.getOrNull()

    override val currentLocale: ILocale
        get() = Locale.getDefault().toILocale()

    private val allAppsState = mutableStateListFlow {
        mutableListOf<IApp>().apply {
            userHandles?.forEach { handle ->
                launcherApps?.getActivityList(null, handle)?.map { aInfo ->
                    App(androidContext, aInfo)
                }?.also { apps ->
                    addAll(apps)
                }
            }
        }
    }

    override val allApps: List<IApp>
        get() = runBlocking {
            allAppsState.firstOrNull() ?: emptyList()
        }

    override val categories: List<Category>
        get() = allApps
            .asSequence()
            .flatMap { app -> app.categories }
            .distinct()
            .toList()
            .sortedBy { c -> c.name }
            .sortedByDescending { c -> c.priority }

    override val favoriteApps: MutableList<IApp> = mutableListOf()

    override suspend fun startApp(app: IApp) {
        favoriteApps.addIfNotExists(app)
        app.start()
    }
}

private fun Locale.toILocale(): ILocale = ILocale.from(displayCountry, displayLanguage)

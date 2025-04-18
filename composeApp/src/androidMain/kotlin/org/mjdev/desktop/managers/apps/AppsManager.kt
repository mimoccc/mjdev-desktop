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
import android.os.Environment
import android.os.UserHandle
import android.os.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import org.mjdev.desktop.context.DesktopContext
import org.mjdev.desktop.data.App
import org.mjdev.desktop.extensions.MutableStateExt.mutableStateListFlow
import org.mjdev.desktop.extensions.PathExt.filesOnly
import org.mjdev.desktop.extensions.PathExt.sortedByName
import org.mjdev.desktop.interfaces.IDesktopContext
import org.mjdev.desktop.interfaces.ILocale
import java.util.Locale

@Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName")
class AppsManager(
    private val context: IDesktopContext,
    private val scope: CoroutineScope = context.scope
) : IAppsManager {
    val TAG = AppsManager::class.simpleName

    private val androidContext by lazy {
        (context as? DesktopContext)?.context
    }
    private val userManger by lazy {
        runCatching {
            androidContext?.getSystemService(Context.USER_SERVICE) as UserManager
        }.getOrNull()
    }
    private val launcherApps by lazy {
        runCatching {
            androidContext?.getSystemService(LauncherApps::class.java)
        }.getOrNull()
    }

    val userHandles: List<UserHandle>?
        get() = userManger?.getUserProfiles()

    override val currentLocale: ILocale
        get() = Locale.getDefault().toILocale()

    private val allAppsState = mutableStateListFlow {
        mutableListOf<App>().apply {
            userHandles?.forEach { handle ->
                launcherApps?.getActivityList(null, handle)?.map { aInfo ->
                    App(androidContext, aInfo)
                }?.also { apps ->
                    addAll(apps)
                }
            }
        }
    }

    override val allApps: List<App>
        get() = runBlocking {
            allAppsState.firstOrNull() ?: emptyList()
        }

    override val categories
        get() = allApps.asSequence().flatMap { app ->
            app.categories
        }.distinct().toList().sortedBy { c ->
            c.name
        }.sortedByDescending { c ->
            c.priority
        }.toList()

    override val favoriteApps: List<App>
        get() = emptyList()
}

private fun Locale.toILocale(): ILocale = ILocale.from(displayCountry, displayLanguage)



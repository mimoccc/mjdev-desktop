package org.mjdev.desktop.context

import androidx.compose.ui.unit.DpSize
import coil3.ImageLoader
import coil3.PlatformContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.mjdev.desktop.data.Category
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.interfaces.IDesktopContext
import org.mjdev.desktop.interfaces.ILocale
import org.mjdev.desktop.interfaces.IPalette
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.managers.ai.IAiManager
import org.mjdev.desktop.managers.apps.IAppsManager
import org.mjdev.desktop.managers.base.IDelegate
import org.mjdev.desktop.managers.connectivity.IConnectivityManager
import org.mjdev.desktop.managers.os.IOSManager
import org.mjdev.desktop.managers.process.IProcessManager
import org.mjdev.desktop.managers.theme.IThemeManager
import org.mjdev.desktop.managers.translations.ITranslator
import kotlin.reflect.KClass

open class EmptyDesktopContext : IDesktopContext() {
    override val osManager: IOSManager = IOSManager.EMPTY
    override val ai: IAiManager = IAiManager.EMPTY
    override val themeManager: IThemeManager = IThemeManager.EMPTY
    override val translator: ITranslator = ITranslator.EMPTY
    override val appsManager: IAppsManager = IAppsManager.EMPTY
    override val processManager: IProcessManager = IProcessManager.EMPTY
    override val connectionManager: IConnectivityManager = IConnectivityManager.EMPTY

    override val palette: IPalette = IPalette.EMPTY
    override val theme: ITheme = ITheme.DEFAULT

    override var isFirstStart: Boolean = false
    override var isInstalled: Boolean = false
    override val isDebug: Boolean = true
    override val isLandscapeMode: Boolean = true

    override val machineName: String = ""
    override val appArgs: List<String> = emptyList()
    override val containerSize: DpSize = DpSize.Zero
    override val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    override val currentLocale: ILocale = ILocale.DEFAULT
    override val appCategories: List<Category> = emptyList()
    override val allApps: List<IApp> = emptyList()
    override val favoriteApps: List<IApp> = emptyList()

    override val currentUser: IUser = IUser.DEFAULT // todo non null
    override val imageLoader: ImageLoader? = null // todo non null

    override val platformContext: PlatformContext? = null

    override suspend fun open(what: Any?) {}
    override suspend fun logOut() {}
    override suspend fun restart() {}
    override suspend fun suspend() {}
    override suspend fun shutdown() {}
    override suspend fun login(uName: String, uPasswd: String): Boolean  = false
    override suspend fun logout(): Boolean = false

    override fun createManager(cls: KClass<*>): IDelegate {
        throw (RuntimeException("No manager found for: $cls"))
    }

    override fun dispose() {}
}

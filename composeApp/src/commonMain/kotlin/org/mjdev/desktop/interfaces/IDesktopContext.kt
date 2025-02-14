package org.mjdev.desktop.interfaces

import androidx.compose.ui.unit.DpSize
import coil3.ImageLoader
import coil3.PlatformContext
import kotlinx.coroutines.CoroutineScope
import org.mjdev.desktop.components.controlcenter.pages.about.AboutPage
import org.mjdev.desktop.components.controlcenter.pages.ai.AIPage
import org.mjdev.desktop.components.controlcenter.pages.bluetooth.BluetoothSettingsPage
import org.mjdev.desktop.components.controlcenter.pages.devices.DevicesPage
import org.mjdev.desktop.components.controlcenter.pages.display.DisplaySettingsPage
import org.mjdev.desktop.components.controlcenter.pages.ethernet.EthSettingsPage
import org.mjdev.desktop.components.controlcenter.pages.main.MainSettingsPage
import org.mjdev.desktop.components.controlcenter.pages.remotes.RemotesSettingsPage
import org.mjdev.desktop.components.controlcenter.pages.sound.SoundSettingsPage
import org.mjdev.desktop.components.controlcenter.pages.theme.ThemeSettingsPage
import org.mjdev.desktop.components.controlcenter.pages.wifi.WifiSettingsPage
import org.mjdev.desktop.data.Category
import org.mjdev.desktop.managers.ai.IAiManager
import org.mjdev.desktop.managers.apps.IAppsManager
import org.mjdev.desktop.managers.connectivity.IConnectivityManager
import org.mjdev.desktop.managers.os.IOSManager
import org.mjdev.desktop.managers.process.IProcessManager
import org.mjdev.desktop.managers.theme.IThemeManager
import org.mjdev.desktop.managers.translations.ITranslator
import org.mjdev.desktop.managers.base.IDelegate
import org.mjdev.desktop.managers.base.ManagerCache
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class IDesktopContext : IDisposable {
    open val managersCache: ManagerCache by lazy {
        ManagerCache(this)
    }

    open val controlCenterPages: List<IPage> = CONTROL_CENTER_PAGES

    open val osManager: IOSManager by this
    open val connectionManager: IConnectivityManager by this
    open val ai: IAiManager by this
    open val appsManager: IAppsManager by this
    open val themeManager: IThemeManager by this
    open val processManager: IProcessManager by this
    open val palette: IPalette by this
    open val translator: ITranslator by this

    abstract var isFirstStart: Boolean
    abstract var isInstalled: Boolean
    abstract val machineName: String
    abstract val isLandscapeMode: Boolean
    abstract val appArgs: List<String>
    abstract val containerSize: DpSize
    abstract val scope: CoroutineScope
    abstract val currentUser: IUser
    abstract val imageLoader: ImageLoader? // todo manager
    abstract val currentLocale: ILocale
    abstract val theme: ITheme
    abstract val appCategories: List<Category>
    abstract val allApps: List<IApp>
    abstract val favoriteApps: List<IApp>

    open val isDebug: Boolean
        get() = appArgs.contains(APP_PARAM_DEBUG)

    abstract val platformContext: PlatformContext?

    abstract suspend fun open(what: Any?)
    abstract suspend fun logOut()
    abstract suspend fun restart()
    abstract suspend fun suspend()
    abstract suspend fun shutdown()
    abstract suspend fun login(uName: String, uPasswd: String): Boolean
    abstract suspend fun logout(): Boolean

    abstract fun createManager(cls: KClass<*>): IDelegate?

    companion object {
        const val APP_PARAM_DEBUG = "--debug"
        val CONTROL_CENTER_PAGES = listOf<IPage>(
            MainSettingsPage(),
            WifiSettingsPage(),
            EthSettingsPage(),
            SoundSettingsPage(),
            BluetoothSettingsPage(),
            DisplaySettingsPage(),
            DevicesPage(),
            RemotesSettingsPage(),
            ThemeSettingsPage(),
            AIPage(),
            AboutPage()
        )
    }

    inline operator fun <reified T : IDelegate> IDesktopContext.getValue(
        desktopContext: IDesktopContext,
        property: KProperty<*>
    ): T = managersCache.get()
}


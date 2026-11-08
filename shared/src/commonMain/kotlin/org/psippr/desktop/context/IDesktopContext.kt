package org.psippr.desktop.context

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.DpSize
import coil3.ImageLoader
import coil3.PlatformContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.psippr.desktop.components.controlcenter.pages.about.AboutPage
import org.psippr.desktop.components.controlcenter.pages.ai.AIPage
import org.psippr.desktop.components.controlcenter.pages.bluetooth.BluetoothSettingsPage
import org.psippr.desktop.components.controlcenter.pages.desktop.DesktopSettingsPage
import org.psippr.desktop.components.controlcenter.pages.devices.DevicesPage
import org.psippr.desktop.components.controlcenter.pages.display.DisplaySettingsPage
import org.psippr.desktop.components.controlcenter.pages.ethernet.EthSettingsPage
import org.psippr.desktop.components.controlcenter.pages.main.MainSettingsPage
import org.psippr.desktop.components.controlcenter.pages.plugins.PluginsSettingsPage
import org.psippr.desktop.components.controlcenter.pages.remotes.RemotesSettingsPage
import org.psippr.desktop.components.controlcenter.pages.sound.SoundSettingsPage
import org.psippr.desktop.components.controlcenter.pages.theme.ThemeSettingsPage
import org.psippr.desktop.components.controlcenter.pages.wallpaper.WallpaperSettingsPage
import org.psippr.desktop.components.controlcenter.pages.wifi.WifiSettingsPage
import org.psippr.desktop.data.Category
import org.psippr.desktop.helpers.persistence.StorageProvider
import org.psippr.desktop.interfaces.IApp
import org.psippr.desktop.interfaces.IDisposable
import org.psippr.desktop.interfaces.ILocale
import org.psippr.desktop.interfaces.IPage
import org.psippr.desktop.interfaces.ITheme
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.managers.ai.IAiManager
import org.psippr.desktop.managers.apps.IAppsManager
import org.psippr.desktop.managers.base.IDelegate
import org.psippr.desktop.managers.base.ManagerCache
import org.psippr.desktop.managers.connectivity.IConnectivityManager
import org.psippr.desktop.managers.devices.IDevicesManager
import org.psippr.desktop.managers.display.IDisplayManager
import org.psippr.desktop.managers.keys.IKeyManager
import org.psippr.desktop.managers.notifications.INotificationsManager
import org.psippr.desktop.managers.os.IOSManager
import org.psippr.desktop.managers.palette.IPalette
import org.psippr.desktop.managers.plugins.IPluginManager
import org.psippr.desktop.managers.process.IProcessManager
import org.psippr.desktop.managers.remote.IRemoteDesktopManager
import org.psippr.desktop.managers.theme.IThemeManager
import org.psippr.desktop.managers.translations.ITranslator
import org.psippr.desktop.managers.volume.IVolumeManager
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("LeakingThis", "unused")
abstract class IDesktopContext : IDisposable {
    open val managersCache: ManagerCache = ManagerCache(this)

    open val controlCenterPages: MutableList<IPage> =
        mutableListOf(
            MainSettingsPage(this),
            WifiSettingsPage(this),
            EthSettingsPage(this),
            SoundSettingsPage(this),
            BluetoothSettingsPage(this),
            DisplaySettingsPage(this),
            DesktopSettingsPage(this),
            WallpaperSettingsPage(this),
            DevicesPage(this),
            RemotesSettingsPage(this),
            // theme palette moved to WallpaperSettingsPage
            PluginsSettingsPage(this),
            AIPage(this),
            AboutPage(this),
        )

    val storageProvider = StorageProvider(this)

    /**
     * Source of the wallpaper the desktop currently shows (path, url, bitmap or a plain color), or
     * null before the first one is chosen. Other shell windows read it to draw frosted glass.
     */
    val wallpaper: MutableState<Any?> = mutableStateOf(null)

    open val osManager: IOSManager by this
    open val connectionManager: IConnectivityManager by this
    open val ai: IAiManager by this
    open val appsManager: IAppsManager by this
    open val themeManager: IThemeManager by this
    open val processManager: IProcessManager by this
    open val palette: IPalette by this
    open val translator: ITranslator by this
    open val keysManager: IKeyManager by this
    open val remoteDesktop: IRemoteDesktopManager by this

    // System output volume / mute control - see IVolumeManager for platform implementations.
    open val volumeManager: IVolumeManager by this

    // Screen resolution of the main display - see IDisplayManager for platform implementations.
    open val displayManager: IDisplayManager by this

    // Connected phones / tablets (ADB first, more providers later) - see IDevicesManager.
    open val devicesManager: IDevicesManager by this

    // Notifications: popup + history on the control center home tab - see INotificationsManager.
    open val notificationsManager: INotificationsManager by this

    // Discovers and toggles desktop widget plugins from ~/.psippr-desktop/plugins - see IPluginManager.
    open val pluginManager: IPluginManager by this

    abstract var isFirstStart: Boolean
    abstract var isInstalled: Boolean

    abstract val machineName: String
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

    /**
     * Opens the file or folder [path] with the app associated to its type, when one is; tells the
     * user with a notification otherwise. Returns at once. No-op where unsupported.
     */
    open fun openFile(path: String) {}

    abstract suspend fun logOut()

    abstract suspend fun restart()

    abstract suspend fun suspend()

    abstract suspend fun shutdown()

    abstract suspend fun login(
        uName: String,
        uPasswd: String,
    ): Boolean

    abstract suspend fun logout(): Boolean

    abstract fun createManager(cls: KClass<*>): IDelegate?

    fun runAsync(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit,
    ) = scope.launch(context, start, block)

    /**
     * Shows a notification (popup, then kept in the home tab history). [id] is not used any more:
     * every notification gets its own UUID.
     */
    fun notify(
        message: String = "This is a test message",
        id: String = "1",
        title: String = "This is a test",
    ) {
        notificationsManager.notify(title = title, message = message)
//        val messageData = KNotifMessageData(
//            id = id,
//            title = title,
//            appName = stringResource(Res.string.app_name),
//            message = message,
//            poster = imageResource(Res.drawable.default_poster),
//            appIcon = imageResource(Res.drawable.default_app_icon),
//        )
//        // Show the notification
//        Knotif.show(messageData)
//        // Dismiss the notification
//        Knotif.dismiss(messageData.id)
//        // Dismiss all notifications
//        Knotif.dismissAll()
//        // Set a listener to be called when a notification is clicked
//        Knotif.setOnBuildMessageKnotifListener {
//            println("notification clicked ${it}")
//        }
    }

    companion object {
        const val APP_PARAM_DEBUG = "--debug"
    }

    inline operator fun <reified T : IDelegate> IDesktopContext.getValue(
        desktopContext: IDesktopContext,
        property: KProperty<*>,
    ): T = managersCache.get()
}

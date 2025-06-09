package org.mjdev.desktop.context

import androidx.compose.ui.unit.DpSize
import coil3.ImageLoader
import coil3.PlatformContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import org.mjdev.desktop.helpers.persistence.StorageProvider
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.interfaces.IDisposable
import org.mjdev.desktop.interfaces.ILocale
import org.mjdev.desktop.interfaces.IPage
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.managers.ai.IAiManager
import org.mjdev.desktop.managers.apps.IAppsManager
import org.mjdev.desktop.managers.connectivity.IConnectivityManager
import org.mjdev.desktop.managers.os.IOSManager
import org.mjdev.desktop.managers.process.IProcessManager
import org.mjdev.desktop.managers.theme.IThemeManager
import org.mjdev.desktop.managers.translations.ITranslator
import org.mjdev.desktop.managers.base.IDelegate
import org.mjdev.desktop.managers.base.ManagerCache
import org.mjdev.desktop.managers.keys.IKeyManager
import org.mjdev.desktop.managers.palette.IPalette
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("LeakingThis", "unused")
abstract class IDesktopContext : IDisposable {
    open val managersCache: ManagerCache = ManagerCache(this)

    open val controlCenterPages: MutableList<IPage> = mutableListOf(
        MainSettingsPage(this),
        WifiSettingsPage(this),
        EthSettingsPage(this),
        SoundSettingsPage(this),
        BluetoothSettingsPage(this),
        DisplaySettingsPage(this),
        DevicesPage(this),
        RemotesSettingsPage(this),
        ThemeSettingsPage(this),
        AIPage(this),
        AboutPage(this)
    )

    val storageProvider = StorageProvider(this)

    open val osManager: IOSManager by this
    open val connectionManager: IConnectivityManager by this
    open val ai: IAiManager by this
    open val appsManager: IAppsManager by this
    open val themeManager: IThemeManager by this
    open val processManager: IProcessManager by this
    open val palette: IPalette by this
    open val translator: ITranslator by this
    open val keysManager: IKeyManager by this

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
    abstract suspend fun logOut()
    abstract suspend fun restart()
    abstract suspend fun suspend()
    abstract suspend fun shutdown()
    abstract suspend fun login(uName: String, uPasswd: String): Boolean
    abstract suspend fun logout(): Boolean

    abstract fun createManager(cls: KClass<*>): IDelegate?

    fun runAsync(
        context: CoroutineContext = Dispatchers.Default,
        block: suspend () -> Unit
    ) = scope.launch(context) { block() }

    fun notify(
        message: String = "This is a test message",
        id: String = "1",
        title: String = "This is a test",
    ) {
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
        property: KProperty<*>
    ): T = managersCache.get()
}

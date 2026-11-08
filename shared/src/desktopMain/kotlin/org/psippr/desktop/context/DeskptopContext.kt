package org.psippr.desktop.context

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.Bitmap
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okio.Path
import org.psippr.desktop.data.Category
import org.psippr.desktop.data.User
import org.psippr.desktop.extensions.Compose.isDesign
import org.psippr.desktop.helpers.application.ApplicationScope
import org.psippr.desktop.helpers.files.FileOpener
import org.psippr.desktop.helpers.image.ImageLoader.asyncImageLoader
import org.psippr.desktop.helpers.system.shell.Shell
import org.psippr.desktop.interfaces.IApp
import org.psippr.desktop.interfaces.ITheme
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.log.Log
import org.psippr.desktop.managers.ai.AiManager
import org.psippr.desktop.managers.ai.IAiManager
import org.psippr.desktop.managers.ai.plugins.AiPluginGemini
import org.psippr.desktop.managers.ai.plugins.AiPluginOpenAi
import org.psippr.desktop.managers.ai.stt.STTPluginEmpty
import org.psippr.desktop.managers.ai.tts.TTSPluginSwift
import org.psippr.desktop.managers.apps.AppsManager
import org.psippr.desktop.managers.apps.IAppsManager
import org.psippr.desktop.managers.base.IDelegate
import org.psippr.desktop.managers.connectivity.ConnectivityManager
import org.psippr.desktop.managers.connectivity.IConnectivityManager
import org.psippr.desktop.managers.devices.DevicesManager
import org.psippr.desktop.managers.devices.IDevicesManager
import org.psippr.desktop.managers.display.DisplayManager
import org.psippr.desktop.managers.display.IDisplayManager
import org.psippr.desktop.managers.keys.IKeyManager
import org.psippr.desktop.managers.keys.KeysManager
import org.psippr.desktop.managers.language.Translator
import org.psippr.desktop.managers.notifications.INotificationsManager
import org.psippr.desktop.managers.notifications.NotificationsManager
import org.psippr.desktop.managers.os.IOSManager
import org.psippr.desktop.managers.os.OsManager
import org.psippr.desktop.managers.palette.IPalette
import org.psippr.desktop.managers.palette.Palette
import org.psippr.desktop.managers.plugins.IPluginManager
import org.psippr.desktop.managers.plugins.PluginManager
import org.psippr.desktop.managers.process.IProcessManager
import org.psippr.desktop.managers.processes.ProcessManager
import org.psippr.desktop.managers.remote.IRemoteDesktopManager
import org.psippr.desktop.managers.remote.RemoteDesktopManager
import org.psippr.desktop.managers.theme.IThemeManager
import org.psippr.desktop.managers.theme.ThemeManager
import org.psippr.desktop.managers.translations.ITranslator
import org.psippr.desktop.managers.volume.IVolumeManager
import org.psippr.desktop.managers.volume.VolumeManager
import java.awt.Desktop
import java.awt.Toolkit
import java.io.File
import java.net.URI
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.jvm.isAccessible

@Suppress("RedundantSuspendModifier", "unused", "MemberVisibilityCanBePrivate")
class DesktopContext(
    val application: ApplicationScope? = null,
    override val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    override val imageLoader: ImageLoader? = null,
    val isInDesign: Boolean = false,
    override val platformContext: PlatformContext? = null,
) : IDesktopContext() {
    private val toolkit: Toolkit by lazy { Toolkit.getDefaultToolkit() }
    private val desktopUtils: Desktop by lazy { Desktop.getDesktop() }
    //    val windowsManager by lazy { WindowsManager(this) }
    //    val dbus: DBus by lazy { DBus() }

    override var isFirstStart: Boolean = true // todo
    override var isInstalled: Boolean = false // todo
    override val appArgs: List<String>
        get() = application?.args ?: emptyList()

    override val isDebug: Boolean
        get() = appArgs.contains(APP_PARAM_DEBUG) || isInDesign

    val allUsers
        get() = User.allUsers(this)
    override val currentLocale
        get() = appsManager.currentLocale
    override val machineName
        get() = osManager.machineName
    override val allApps: List<IApp>
        get() = appsManager.allApps
    override val appCategories: List<Category>
        get() = appsManager.categories
    override val favoriteApps: List<IApp>
        get() = appsManager.favoriteApps

    override suspend fun open(what: Any?) {
        open(what.toString())
    }

    override fun openFile(path: String) {
        runAsync(Dispatchers.IO) {
            val result = FileOpener.open(path)
            if (result.message.isNotBlank()) notify(title = File(path).name, message = result.message)
        }
    }

    // logged-in user, remembered once found: the process user cannot change while running, and
    // re-resolving on every access (reads /etc/passwd + runs whoami) fell back to User.DEFAULT
    // whenever whoami failed once, which switched the desktop config and wiped the wallpapers
    private var resolvedUser: IUser? = null

    // todo state
    override val currentUser: IUser
        get() = resolvedUser ?: resolveLoggedInUser()

    // finds the logged-in user; only a real match is remembered, User.DEFAULT is retried next time
    private fun resolveLoggedInUser(): IUser = allUsers
        .firstOrNull { u -> u.isLoggedIn }
        ?.also { user -> resolvedUser = user }
        ?: User.DEFAULT

    override val theme: ITheme
        get() = currentUser.theme
    override val containerSize: DpSize
        get() =
            runCatching {
                toolkit.screenSize.let { screen -> DpSize(screen.width.dp, screen.height.dp) }
            }.getOrNull() ?: DpSize.Zero

    init {
        // sudo apt-get update
        // sudo apt-get install \
        //    libgl1-mesa-dev \
        //    xorg-dev \
        //    libgtk-3-dev \
        //    libglib2.0-dev \
        //    libwebkit2gtk-4.0-dev \
        //    libjavafx-web-dev \
        //    openjfx \
        //    libwebkit2gtk-4.0-37 \
        //    libwebkitgtk-6.0-4
//        val javaLibPath = System.getProperty("java.library.path")
//        System.setProperty("prism.order", "sw")
//        System.setProperty("glass.platform", "gtk")
//        System.setProperty("javafx.platform", "gtk")
//        System.setProperty("quantum.multithreaded", "false")
//        System.setProperty("java.library.path","$javaLibPath:/usr/lib/x86_64-linux-gnu/")
//        Log.i("Got app args: $args")
//        runCatching {
//            windowsManager.init()
//        }
//        runCatching {
//            mounts.init()
//        }
//        runCatching {
//            dbus.updateEnvironment()
//        }
    }

    override fun dispose() {
        Log.i("Cleaning up resources.")
        processManager.dispose()
        palette.dispose()
        theme.dispose()
//        runCatching {
//            mounts.dispose()
//        }
//        runCatching {
//            kcefHelper.dispose()
//        }
//        runCatching {
//            windowsManager.dispose()
//        }
    }

    suspend fun openMail(emailAddress: String) = runCatching {
        desktopUtils.mail(URI.create("mailto:$emailAddress"))
    }

    suspend fun openBrowser(url: String) = runCatching {
        desktopUtils.browse(URI.create(url))
    }

    suspend fun openDirectoryForFile(path: String) = runCatching {
        desktopUtils.browseFileDirectory(File(path))
    }

    suspend fun moveToTrash(file: File) = runCatching {
        desktopUtils.moveToTrash(file)
    }

    suspend fun moveToTrash(filePath: String) = runCatching {
        moveToTrash(File(filePath))
    }

    // todo, may be need another function
    suspend fun open(what: String) {
        Shell.executeAndRead("xdg-open", what)
    }

    suspend fun openFileInAssociated(file: File) = runCatching {
        desktopUtils.open(file)
    }

    suspend fun openFileInAssociated(filePath: String) = runCatching {
        openFileInAssociated(File(filePath))
    }

//    suspend fun beep() = runCatching {
//        toolkit.beep()
//    }

//    suspend fun sync() = runCatching {
//        toolkit.sync()
//    }

//    suspend fun createCustomCursor(cursor: Image, hotSpot: Point, name: String): Cursor? = runCatching {
//        toolkit.createCustomCursor(cursor, hotSpot, name)
//    }.getOrNull()

//    suspend fun createCustomCursor(cursor: Bitmap, hotSpot: Point, name: String): Cursor? = runCatching {
//        toolkit.createCustomCursor(cursor.image, hotSpot, name)
//    }.getOrNull()

//    suspend fun createCustomCursor(cursor: ImageBitmap, hotSpot: Point, name: String): Cursor? = runCatching {
//        toolkit.createCustomCursor(cursor.toAwtImage(), hotSpot, name)
//    }.getOrNull()

//    suspend fun createCustomCursor(cursor: ImageVector, hotSpot: Point, name: String) =
//        toolkit.createCustomCursor(cursor, hotSpot, name)

//    @Suppress("UNUSED_PARAMETER")
//    override suspend fun login(
//        uname: String,
//        upasswd: String
//    ): Boolean = true  // todo

//    override suspend fun logout() : Boolean = true // todo

    fun lock() = runAsync {
        Shell.executeAndRead("/usr/bin/loginctl", "lock-sessions")
    }

    override suspend fun logOut() {
        runAsync {
            application?.exitApplication()
        }
    }

    override suspend fun suspend() {
        Shell.executeAndRead("/usr/bin/systemctl", "suspend")
    }

    override suspend fun shutdown() {
        Shell.executeAndRead("/usr/sbin/halt", "--poweroff")
    }

    override suspend fun login(
        uName: String,
        uPasswd: String,
    ): Boolean {
        return false // todo
    }

    override suspend fun logout(): Boolean {
        return false // todo
    }

    override fun createManager(cls: KClass<*>): IDelegate = when (cls) {
        IOSManager::class -> OsManager(this)
        IPalette::class -> Palette(this)
        ITranslator::class -> Translator(this)
        IConnectivityManager::class -> ConnectivityManager(this)
        IAiManager::class ->
            AiManager(
                context = this,
                // todo user can configure
                pluginAI = AiPluginOpenAi(this@DesktopContext),
                pluginTTS = TTSPluginSwift(this@DesktopContext),
                pluginSTT = STTPluginEmpty(this@DesktopContext),
            )

        IAppsManager::class -> AppsManager(this)
        IThemeManager::class -> ThemeManager(this)
        IProcessManager::class -> ProcessManager(this)
        IKeyManager::class -> KeysManager(this)
        IRemoteDesktopManager::class -> RemoteDesktopManager(this)
        IVolumeManager::class -> VolumeManager(this)
        IPluginManager::class -> PluginManager(this)
        IDisplayManager::class -> DisplayManager(this)
        IDevicesManager::class -> DevicesManager(this)
        INotificationsManager::class -> NotificationsManager(this)
        else -> createEmptyManager(cls)
    }

    /**
     * Resolves the `EMPTY` fallback of a manager's companion. The backing field is private
     * static, so the getter must be made accessible before it is called reflectively.
     */
    private fun createEmptyManager(cls: KClass<*>): IDelegate = runCatching {
        cls.companionObject
            ?.members
            ?.firstOrNull { it.name == "EMPTY" }
            ?.also { it.isAccessible = true }
            ?.call(cls.companionObjectInstance) as? IDelegate
    }.onFailure { e ->
        Log.e(e)
    }.getOrNull() ?: error("No manager available for $cls")

    override suspend fun restart() {
        Shell.executeAndRead("/usr/sbin/halt", "--reboot")
    }

//    suspend fun startApp(app: App) {
//        startApp(app.desktopFile)
//    }

//    suspend fun Desktop.startApp(
//        desktopFile: DesktopFile?
//    ) = withContext(Dispatchers.Default) {
//        open(desktopFile?.file)
//    }

    companion object {
        // todo application
        @Composable
        fun rememberDesktopContext(
            application: ApplicationScope?,
            imageLoader: ImageLoader = asyncImageLoader(),
            platformContext: PlatformContext = LocalPlatformContext.current,
            scope: CoroutineScope = rememberCoroutineScope(),
            isInDesign: Boolean = isDesign,
        ) = remember {
            Log.i("default initialized desktop provider created")
            DesktopContext(
                application = application,
                scope = scope,
                imageLoader = imageLoader,
                isInDesign = isInDesign,
                platformContext = platformContext,
            )
        }

        suspend fun IDesktopContext.loadPicture(src: Any?): Bitmap? = runCatching {
            ImageRequest
                .Builder(platformContext!!)
                .data(
                    when (src) {
                        is Path -> src.toFile()
                        else -> src.toString()
                    },
                ).build()
                .let { req ->
                    imageLoader?.execute(req)?.image?.toBitmap()
                }
        }.getOrNull()
    }
}

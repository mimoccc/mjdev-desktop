package org.mjdev.desktop.context

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
import kotlinx.coroutines.withContext
import okio.Path
import org.mjdev.desktop.data.Category
import org.mjdev.desktop.data.User
import org.mjdev.desktop.extensions.Compose.isDesign
import org.mjdev.desktop.helpers.application.ApplicationScope
import org.mjdev.desktop.helpers.image.ImageLoader.asyncImageLoader
import org.mjdev.desktop.managers.palette.Palette
import org.mjdev.desktop.helpers.system.shell.Shell
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.managers.connectivity.IConnectivityManager
import org.mjdev.desktop.managers.palette.IPalette
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.managers.language.Translator
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.managers.ai.AiManager
import org.mjdev.desktop.managers.ai.IAiManager
import org.mjdev.desktop.managers.ai.plugins.AiPluginGemini
import org.mjdev.desktop.managers.ai.plugins.AiPluginOpenAi
import org.mjdev.desktop.managers.ai.stt.STTPluginEmpty
import org.mjdev.desktop.managers.ai.tts.TTSPluginSwift
import org.mjdev.desktop.managers.apps.IAppsManager
import org.mjdev.desktop.managers.os.IOSManager
import org.mjdev.desktop.managers.process.IProcessManager
import org.mjdev.desktop.managers.theme.IThemeManager
import org.mjdev.desktop.managers.translations.ITranslator
import org.mjdev.desktop.managers.apps.AppsManager
import org.mjdev.desktop.managers.base.IDelegate
import org.mjdev.desktop.managers.connectivity.ConnectivityManager
import org.mjdev.desktop.managers.keys.IKeyManager
import org.mjdev.desktop.managers.os.OsManager
import org.mjdev.desktop.managers.processes.ProcessManager
import org.mjdev.desktop.managers.window.IWindowsManager
import org.mjdev.desktop.managers.window.WindowsManager
import org.mjdev.desktop.managers.theme.ThemeManager
import java.awt.Desktop
import java.awt.Toolkit
import java.io.File
import java.net.URI
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import org.mjdev.desktop.managers.keys.KeysManager

@Suppress("RedundantSuspendModifier", "unused", "MemberVisibilityCanBePrivate")
class DesktopContext(
    val application: ApplicationScope? = null,
    override val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    override val imageLoader: ImageLoader? = null,
    val isInDesign: Boolean = false,
    override val platformContext: PlatformContext? = null
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

    // todo state
    override val currentUser: IUser
        get() = allUsers.firstOrNull { u -> u.isLoggedIn } ?: User.DEFAULT
    override val theme: ITheme
        get() = currentUser.theme
    override val containerSize: DpSize
        get() = runCatching {
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
        runCatching {
            windowsManager.dispose()
        }
//        runCatching {
//            mounts.dispose()
//        }
//        runCatching {
//            kcefHelper.dispose()
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

    /**
     * verifies the password of a local user against PAM via the
     * unix_chkpwd helper (the same mechanism screen lockers use);
     * works for the calling user without root and without extra deps
     */
    override suspend fun login(uName: String, uPasswd: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val helper = sequenceOf(
                    "/usr/sbin/unix_chkpwd",
                    "/sbin/unix_chkpwd",
                ).firstOrNull { path -> File(path).canExecute() }
                    ?: return@runCatching false
                val process = ProcessBuilder(helper, uName, "nullok").start()
                process.outputStream.use { out ->
                    out.write(uPasswd.toByteArray())
                    out.write(0)
                }
                process.waitFor() == 0
            }.getOrDefault(false)
        }

    override suspend fun logout(): Boolean {
        authenticatedState.value = false
        return true
    }

    override fun createManager(cls: KClass<*>): IDelegate = when (cls) {
        IOSManager::class -> OsManager(this)
        IPalette::class -> Palette(this)
        ITranslator::class -> Translator(this)
        IConnectivityManager::class -> ConnectivityManager(this)
        IAiManager::class -> AiManager(
            context = this,
            // todo user can configure
            pluginAI = AiPluginOpenAi(this@DesktopContext),
            pluginTTS = TTSPluginSwift(this@DesktopContext),
            pluginSTT = STTPluginEmpty(this@DesktopContext)
        )

        IAppsManager::class -> AppsManager(this)
        IThemeManager::class -> ThemeManager(this)
        IProcessManager::class -> ProcessManager(this)
        IKeyManager::class -> KeysManager(this)
        IWindowsManager::class -> WindowsManager(this)
        else -> cls.companionObject?.members?.first { it.name == "EMPTY" }?.call() as IDelegate
    }

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
                platformContext = platformContext
            )
        }

        suspend fun IDesktopContext.loadPicture(
            src: Any?
        ): Bitmap? = runCatching {
            ImageRequest.Builder(platformContext!!)
                .data(
                    when (src) {
                        is Path -> src.toFile()
                        else -> src.toString()
                    }
                )
                .build().let { req ->
                    imageLoader?.execute(req)?.image?.toBitmap()
                }
        }.getOrNull()
    }
}

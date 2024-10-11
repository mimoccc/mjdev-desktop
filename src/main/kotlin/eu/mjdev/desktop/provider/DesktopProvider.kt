package eu.mjdev.desktop.provider

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import eu.mjdev.desktop.components.controlcenter.pages.*
import eu.mjdev.desktop.data.User
import eu.mjdev.desktop.extensions.Compose.asyncImageLoader
import eu.mjdev.desktop.helpers.internal.Palette
import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.managers.apps.AppsManager
import eu.mjdev.desktop.managers.artificialintelligence.AIManager
import eu.mjdev.desktop.managers.artificialintelligence.AIManager.Companion.aiManager
import eu.mjdev.desktop.managers.artificialintelligence.plugins.AiPluginGemini
import eu.mjdev.desktop.managers.connectivity.ConnectivityManager
import eu.mjdev.desktop.managers.kcef.kcefManager
import eu.mjdev.desktop.managers.os.osManager
import eu.mjdev.desktop.managers.processes.ProcessManager
import eu.mjdev.desktop.managers.theme.themeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.io.File
import java.net.URI
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

@Suppress("unused", "MemberVisibilityCanBePrivate", "SameParameterValue", "RedundantSuspendModifier")
class DesktopProvider(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    val args: List<String> = emptyList(),
    val imageLoader: ImageLoader? = null,
    val isDebug: Boolean = true,
    val isFirstStart: Boolean = true, // todo
    val isInstalled: Boolean = false, // todo
) : AutoCloseable {
    val osManager by lazy { osManager(this) }
    val toolkit: Toolkit by lazy { Toolkit.getDefaultToolkit() }
    val kcefHelper by lazy { kcefManager(this) }
    val connectionManager by lazy { ConnectivityManager() }
    val ai: AIManager by lazy {
        aiManager(this).apply {
            // todo user can configure
            pluginAI = AiPluginGemini(scope)
        }
    }
    val appsManager by lazy { AppsManager(this) }
    val themeManager by lazy { themeManager(this) }
    val palette by lazy { Palette(this) }
    val desktopUtils: Desktop by lazy { Desktop.getDesktop() }
    val processManager by lazy { ProcessManager() }
    //    val windowsManager by lazy { WindowsManager(this) }
    //    val dbus: DBus by lazy { DBus() }

    val homeDir
        get() = currentUser.homeDir
    val allUsers
        get() = User.allUsers
    val currentLocale get() = appsManager.currentLocale
    val machineName
        get() = osManager.machineName

    // todo state
    val currentUser: User
        get() = allUsers.firstOrNull { u -> u.isLoggedIn } ?: User.Nobody

    val controlCenterPagesState = mutableStateOf(CONTROL_CENTER_PAGES)

//    val adbHandler = adbDevicesHandler(
//        coroutineScope = scope,
//        onAdded = { device ->
//            println("Device discovered : $device")
//            // when device connected
//        },
//        onRemoved = { device ->
//            println("Device removed : $device")
//            // when device removed
//        }
//    )

    val containerSize: DpSize
        get() = runCatching {
            toolkit.screenSize.let { screen -> DpSize(screen.width.dp, screen.height.dp) }
        }.getOrNull() ?: DpSize.Zero

    val isLandscapeMode: Boolean
        get() = containerSize.let { it.width > it.height }

    val isAlwaysOnTopSupported
        get() = runCatching {
            toolkit.isAlwaysOnTopSupported
        }.getOrNull() ?: false

    val cursorSize
        get() = toolkit.getBestCursorSize(32, 32).let {
            DpSize(it.width.dp, it.height.dp)
        }

    val controlCenterPages
        get() = controlCenterPagesState.value

    val graphicsEnvironment: GraphicsEnvironment
        get() = GraphicsEnvironment.getLocalGraphicsEnvironment()

    val graphicsDevice: GraphicsDevice
        get() = graphicsEnvironment.defaultScreenDevice

    val isTransparencySupported
        get() = graphicsDevice.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)

//    init {
//        runCatching {
//            windowsManager.init()
//        }
//        runCatching {
//            mounts.init()
//        }
//        runCatching {
//            dbus.updateEnvironment()
//        }
//    }

    override fun close() {
        processManager.close()
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

    fun runAsync(
        context: CoroutineContext = Dispatchers.IO,
        block: suspend () -> Unit
    ) = scope.launch(context) { block() }

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

    suspend fun beep() = runCatching {
        toolkit.beep()
    }

    suspend fun sync() = runCatching {
        toolkit.sync()
    }

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

    @Suppress("UNUSED_PARAMETER")
    suspend fun login(
        user: String,
        password: String
    ): Flow<Boolean> = flow {
        // todo
        emit(true)
    }

    suspend fun lock() {
        Shell.executeAndRead("/usr/bin/loginctl", "lock-sessions")
    }

    suspend fun logOut() {
        exitProcess(0)
    }

    suspend fun suspend() {
        Shell.executeAndRead("/usr/bin/systemctl", "suspend")
    }

    suspend fun shutdown() {
        Shell.executeAndRead("/usr/sbin/halt", "--poweroff")
    }

    suspend fun restart() {
        Shell.executeAndRead("/usr/sbin/halt", "--reboot")
    }

//    suspend fun Desktop.startApp(app: App) {
//        startApp(app.desktopFile)
//    }

//    suspend fun Desktop.startApp(
//        desktopFile: DesktopFile?
//    ) = withContext(Dispatchers.IO) {
//        open(desktopFile?.file)
//    }

    companion object {
        private val CONTROL_CENTER_PAGES = listOf(
            MainSettingsPage(),
            WifiSettingsPage(),
            EthSettingsPage(),
            SoundSettingsPage(),
            BluetoothSettingsPage(),
            DisplaySettingsPage(),
            DevicesPage(),
            AIPage(),
        )

        val LocalDesktop = staticCompositionLocalOf {
            println("default empty desktop provider created")
            DesktopProvider()
        }

        @Composable
        fun rememberDesktopProvider(
            scope: CoroutineScope = rememberCoroutineScope(),
            imageLoader: ImageLoader = asyncImageLoader(),
            isDebug: Boolean = LocalInspectionMode.current,
            args: List<String> = emptyList(),
        ) = remember {
            println("default initialized desktop provider created")
            DesktopProvider(
                scope = scope,
                imageLoader = imageLoader,
                args = args,
                isDebug = isDebug || args.contains("-d")
            )
        }
    }
}

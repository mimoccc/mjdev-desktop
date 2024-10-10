package eu.mjdev.desktop.provider

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import eu.mjdev.desktop.components.controlcenter.pages.*
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.data.DesktopFile
import eu.mjdev.desktop.data.User
import eu.mjdev.desktop.extensions.Compose.asyncImageLoader
import eu.mjdev.desktop.helpers.internal.Palette
import eu.mjdev.desktop.helpers.system.OsRelease
import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.managers.ConnectivityManager
import eu.mjdev.desktop.managers.GnomeManager
import eu.mjdev.desktop.managers.KCEFHelper
import eu.mjdev.desktop.managers.ProcessManager
import eu.mjdev.desktop.provider.AIProvider.AiPluginGemini
import eu.mjdev.desktop.theme.gtk.GtkThemeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.awt.Desktop
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.io.File
import java.net.URI
import kotlin.system.exitProcess

@Suppress("unused", "MemberVisibilityCanBePrivate", "SameParameterValue")
class DesktopProvider(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    val args: List<String> = emptyList(),
    val imageLoader: ImageLoader? = null,
    val isDebug: Boolean = true,
    val isFirstStart: Boolean = true, // todo
    val isInstalled: Boolean = false, // todo
) : AutoCloseable {
    //    val scriptManager: ScriptEngineManager by lazy { ScriptEngineManager() }
//    val scriptEngine: ScriptEngine by lazy {
//        scriptManager.getEngineByName("JavaScript").apply {
//            // todo
//        }
//    }
    val osDetails by lazy { OsRelease() }
    val toolkit: Toolkit by lazy { Toolkit.getDefaultToolkit() }
    val kcefHelper by lazy { KCEFHelper(this) }
    val gnome by lazy { GnomeManager() }
    val connectionManager by lazy { ConnectivityManager() }

    //    val windowsManager by lazy { WindowsManager(this) }
//    val dbus: DBus by lazy { DBus() }
    val ai: AIProvider by lazy {
        // todo user can configure
        AIProvider(scope, AiPluginGemini(scope))
    }
    val appsProvider by lazy { AppsProvider(this) }

    //    val mounts by lazy {
//        FileSystemWatcher(scope) {
//            println("Mounted: ${this.targetDirectory}")
//        }
//    }
    val gtkTheme by lazy { GtkThemeHelper(this) }
    val palette by lazy { Palette(this) }
    val desktopUtils: Desktop by lazy { Desktop.getDesktop() }
    val processManager by lazy { ProcessManager() }

    val homeDir
        get() = currentUser.homeDir
    val allUsers
        get() = User.allUsers
    val machineName
        get() = Shell.executeAndRead("hostname").trim()

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

    fun openMail(emailAddress: String) = runCatching {
        desktopUtils.mail(URI.create("mailto:$emailAddress"))
    }

    fun openBrowser(url: String) = runCatching {
        desktopUtils.browse(URI.create(url))
    }

    fun openDirectoryForFile(path: String) = runCatching {
        desktopUtils.browseFileDirectory(File(path))
    }

    fun moveToTrash(file: File) = runCatching {
        desktopUtils.moveToTrash(file)
    }

    fun moveToTrash(filePath: String) = runCatching {
        moveToTrash(File(filePath))
    }

    fun openFileInAssociated(file: File) = runCatching {
        desktopUtils.open(file)
    }

    fun openFileInAssociated(filePath: String) = runCatching {
        openFileInAssociated(File(filePath))
    }

//    fun runScript(script: String): Any = runCatching {
//        scriptEngine.eval(script)
//    }

//    fun runScript(file: File): Any = runCatching {
//        scriptEngine.eval(FileReader(file))
//    }

    fun beep() = runCatching {
        toolkit.beep()
    }

    fun sync() = runCatching {
        toolkit.sync()
    }

//    fun createCustomCursor(cursor: Image, hotSpot: Point, name: String): Cursor? = runCatching {
//        toolkit.createCustomCursor(cursor, hotSpot, name)
//    }.getOrNull()

//    fun createCustomCursor(cursor: Bitmap, hotSpot: Point, name: String): Cursor? = runCatching {
//        toolkit.createCustomCursor(cursor.image, hotSpot, name)
//    }.getOrNull()

//    fun createCustomCursor(cursor: ImageBitmap, hotSpot: Point, name: String): Cursor? = runCatching {
//        toolkit.createCustomCursor(cursor.toAwtImage(), hotSpot, name)
//    }.getOrNull()

    // todo
//    fun createCustomCursor(cursor: ImageVector, hotSpot: Point, name: String) =
//        toolkit.createCustomCursor(cursor, hotSpot, name)

    @Suppress("UNUSED_PARAMETER")
    fun login(
        user: String,
        password: String
    ): Boolean {
        // todo
        return true
    }

    fun lock() {
        Shell.executeAndRead("/usr/bin/loginctl", "lock-sessions")
    }

    fun logOut() {
        exitProcess(0)
    }

    fun suspend() {
        Shell.executeAndRead("/usr/bin/systemctl", "suspend")
    }

    fun shutdown() {
        Shell.executeAndRead("/usr/sbin/halt", "--poweroff")
    }

    fun restart() {
        Shell.executeAndRead("/usr/sbin/halt", "--reboot")
    }

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

        fun Desktop.startApp(app: App) {
            startApp(app.desktopFile)
        }

        fun Desktop.startApp(
            desktopFile: DesktopFile?
        ) {
            open(desktopFile?.file)
        }
    }
}

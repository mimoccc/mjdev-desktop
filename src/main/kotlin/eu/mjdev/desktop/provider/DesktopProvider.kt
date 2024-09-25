package eu.mjdev.desktop.provider

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage
import eu.mjdev.desktop.components.controlcenter.pages.*
import eu.mjdev.desktop.data.User
import eu.mjdev.desktop.extensions.Compose.asyncImageLoader
import eu.mjdev.desktop.helpers.adb.AdbDiscover.Companion.adbDevicesHandler
import eu.mjdev.desktop.helpers.internal.Palette
import eu.mjdev.desktop.helpers.managers.*
import eu.mjdev.desktop.helpers.system.Command
import eu.mjdev.desktop.helpers.system.OsRelease
import eu.mjdev.desktop.helpers.system.UserDirs
import eu.mjdev.desktop.provider.AIProvider.AiPluginGemini
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.io.File
import kotlin.system.exitProcess

@Suppress("unused", "MemberVisibilityCanBePrivate", "SameParameterValue")
class DesktopProvider(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    val imageLoader: ImageLoader? = null,
    val connection: ConnectivityManager = ConnectivityManager(),
//    val scriptManager: ScriptEngineManager = ScriptEngineManager(),
    val kcefHelper: KCEFHelper = KCEFHelper(scope),
    val ai: AIProvider = AIProvider(scope, AiPluginGemini(scope)),
    val windows: WindowsManager = WindowsManager(),
    val gnome: GnomeManager = GnomeManager(),
    val currentUser: User = User.load() // todo manager
) {
    val homeDir
        get() = File("/home/${currentUser.name}")

    val userDirs = UserDirs(this)

    val osDetails = OsRelease(this)

    val palette: Palette = Palette(scope, currentUser.theme.backgroundColor)

    val controlCenterPagesState: MutableState<List<ControlCenterPage>> = mutableStateOf(CONTROL_CENTER_PAGES)

    val machineName
        get() = Command("hostname").execute()

//    private val engine: ScriptEngine by lazy {
//        scriptManager.getEngineByName("JavaScript").apply {
//            // todo
//        }
//    }

    val adbHandler = adbDevicesHandler(
        coroutineScope = scope
    ) { device ->
        println("Device discovered : $device")
        // when device connected
    }

    val containerSize: DpSize by lazy {
        Toolkit.getDefaultToolkit().screenSize.let {
            DpSize(
                it.width.dp,
                it.height.dp
            )
        }
    }
    val controlCenterPages get() = controlCenterPagesState.value
    val appsProvider = AppsProvider(this)

    val graphicsEnvironment: GraphicsEnvironment
        get() = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val graphicsDevice: GraphicsDevice
        get() = graphicsEnvironment.defaultScreenDevice
    val isTransparencySupported
        get() = graphicsDevice.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)

    val mounts = FileSystemWatcher(scope) {
        println("Mounted: ${this.targetDirectory}")
    }

    init {
        currentUser.theme.controlCenterExpandedWidth = containerSize.width.div(4)
        mounts.init()
    }

    fun dispose() {
        kcefHelper.dispose()
        mounts.dispose()
        windows.dispose()
    }

//    fun runScript(script: String): Any =
//        engine.eval(script)

//    fun runScript(file: File): Any =
//        engine.eval(FileReader(file))

    fun executeCmd(cmd: String) {
        ProcessBuilder().apply {
            command(cmd)
        }.start()
    }

    fun executeCmd(cmd: String, directory: File) {
        ProcessBuilder().apply {
            directory(directory)
            command(cmd)
        }.start()
    }

    @Suppress("UNUSED_PARAMETER")
    fun login(
        user: String,
        password: String
    ) {
        // todo
    }

    fun logOut() {
        // todo
        exitProcess(0)
    }

    fun shutdown() {
        // todo
        exitProcess(0)
    }

    fun restart() {
        // todo
        exitProcess(0)
    }

    // theme support
    class DesktopScope(
        val api: DesktopProvider
    ) {
        val scope
            get() = api.scope
        val backgroundColorState
            get() = api.palette.backgroundColorState
        val backgroundColor
            get() = api.palette.backgroundColor
        val iconsTintColorState
            get() = api.palette.iconsTintColor
        val iconsTintColor
            get() = api.palette.iconsTintColor
        val textColorState
            get() = api.palette.textColorState
        val textColor
            get() = textColorState.value
        val borderColor
            get() = api.palette.borderColor
    }

    companion object {
        private val CONTROL_CENTER_PAGES = listOf(
            MainSettingsPage(),
            EthSettingsPage(),
            WifiSettingsPage(),
            BluetoothSettingsPage(),
            DisplaySettingsPage(),
            SoundSettingsPage(),
            AIPage(),
            DevicesPage()
        )

        val LocalDesktop = compositionLocalOf {
            DesktopProvider()
        }

        @Composable
        fun rememberDesktopProvider(
            scope: CoroutineScope = rememberCoroutineScope(),
            imageLoader: ImageLoader = asyncImageLoader()
        ) = remember {
            DesktopProvider(scope, imageLoader)
        }

        @Composable
        fun withDesktopScope(
            api: DesktopProvider = LocalDesktop.current,
            block: @Composable DesktopScope.() -> Unit
        ) = DesktopScope(api).apply {
            block()
        }
    }
}

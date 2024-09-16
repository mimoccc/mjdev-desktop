package eu.mjdev.desktop.provider

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage
import eu.mjdev.desktop.components.controlcenter.pages.*
import eu.mjdev.desktop.data.User
import eu.mjdev.desktop.helpers.adb.AdbDiscover.Companion.adbDevicesHandler
import eu.mjdev.desktop.helpers.managers.ConnectivityManager
import eu.mjdev.desktop.helpers.managers.FileSystemWatcher
import eu.mjdev.desktop.helpers.managers.KCEFHelper
import eu.mjdev.desktop.provider.AIProvider.AiPluginNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.io.File
import java.io.FileReader
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import kotlin.system.exitProcess

@Suppress("unused", "MemberVisibilityCanBePrivate", "PrivatePropertyName", "SameParameterValue")
class DesktopProvider(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    val imageLoader: ImageLoader? = null,
    val connection: ConnectivityManager = ConnectivityManager(),
    val scriptManager: ScriptEngineManager = ScriptEngineManager(),
    val kcefHelper: KCEFHelper = KCEFHelper(scope),
    val aiProvider: AIProvider = AIProvider(AiPluginNull())
) {
    private val __currentUser: User by lazy { User.load() }
    private val _currentUser: MutableState<User> = mutableStateOf(__currentUser)
    val currentUser
        get() = _currentUser.value

    private val _controlCenterPages: MutableState<List<ControlCenterPage>> = mutableStateOf(CONTROL_CENTER_PAGES)

    private val engine: ScriptEngine by lazy {
        scriptManager.getEngineByName("JavaScript").apply {
            // todo
        }
    }

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
    val controlCenterPages get() = _controlCenterPages.value
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
    }

    fun runScript(script: String): Any =
        engine.eval(script)

    fun runScript(file: File): Any =
        engine.eval(FileReader(file))

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
    }
}

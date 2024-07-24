package eu.mjdev.desktop.provider

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.controlpanel.ControlCenterPage
import eu.mjdev.desktop.components.controlpanel.pages.*
import eu.mjdev.desktop.helpers.WindowFocusState
import eu.mjdev.desktop.provider.data.User
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.util.*
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager


@Suppress("unused")
class DesktopProvider(
    val containerSize: IntSize? = null,
    val currentUser: User = User.Empty,
    val windowFocusState: WindowFocusState = WindowFocusState.EMPTY
) {
    private val _currentUser: MutableState<User> = mutableStateOf(currentUser)

    private val _controlCenterPages: MutableState<List<ControlCenterPage>> =
        mutableStateOf(
            listOf(
                MainSettingsPage(),
                WifiSettingsPage(),
                BluetoothSettingsPage(),
                DisplaySettingsPage(),
                SoundSettingsPage()
            )
        )

    private val scriptManager by lazy { ScriptEngineManager() }
    private val engine: ScriptEngine by lazy {
        scriptManager.getEngineByName("JavaScript").apply {
            // todo
        }
    }

    val controlCenterPages get() = _controlCenterPages.value
    val appsProvider by lazy { AppsProvider(this) }

    init {
        if (containerSize != null) {
            currentUser.theme.controlCenterExpandedWidth = containerSize.width.div(4).dp
        }
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

    fun runCommandForOutput(vararg cmd:String): String {
        val pb = ProcessBuilder(*cmd)
        val p: Process
        var result = ""
        try {
            p = pb.start()
            val reader = BufferedReader(InputStreamReader(p.inputStream))

            val sj = StringJoiner(System.lineSeparator())
            reader.lines().iterator().forEachRemaining { newElement: String? ->
                sj.add(newElement)
            }
            result = sj.toString()
            p.waitFor()
            p.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}

val LocalDesktop = compositionLocalOf {
    DesktopProvider()
}

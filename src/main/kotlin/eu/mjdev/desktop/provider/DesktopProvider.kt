package eu.mjdev.desktop.provider

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.controlpanel.ControlCenterPage
import eu.mjdev.desktop.components.controlpanel.pages.*
import eu.mjdev.desktop.provider.data.User
import java.awt.Toolkit
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.util.*
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

@Suppress("unused")
class DesktopProvider(
    val currentUser: User = User.Empty, // todo
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

    val containerSize: DpSize by lazy {
        Toolkit.getDefaultToolkit().screenSize.let {
            DpSize(
                it.width.dp,
                it.height.dp
            )
        }
    }
    val controlCenterPages get() = _controlCenterPages.value
    val appsProvider by lazy { AppsProvider(this) }

    init {
        currentUser.theme.controlCenterExpandedWidth = containerSize.width.div(4)
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

    fun runCommandForOutput(vararg cmd: String): String? = runCatching {
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
        result
    }.getOrNull()

    companion object {
        val LocalDesktop = compositionLocalOf {
            DesktopProvider()
        }
    }
}

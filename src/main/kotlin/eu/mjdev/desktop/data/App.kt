package eu.mjdev.desktop.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.helpers.exception.EmptyException.Companion.EmptyException
import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.provider.DesktopProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate")
class App(
    val file: File = File("nonexistent_file_path"),
    val desktopFile: DesktopFile = DesktopFile(file),
    val isFavorite: Boolean = false
) {
    val fileName
        get() = desktopFile.fileName
    val type
        get() = desktopFile.type
    val version
        get() = desktopFile.version
    val name
        get() = desktopFile.name
    val comment
        get() = desktopFile.comment
    val path
        get() = desktopFile.path
    val exec
        get() = desktopFile.exec
    val iconName
        get() = desktopFile.icon
    val categories
        get() = desktopFile.categories.ifEmptyCategories { listOf(Category.UNCATEGORIZED) }
    val notifyOnStart
        get() = desktopFile.notifyOnStart
    val runInTerminal
        get() = desktopFile.runInTerminal

    var iconTint: Color? = null
    val iconBackground: Color = Color.White

    val onStopHandler: MutableState<(Throwable) -> Unit> = mutableStateOf({})
    val onStartingHandler: MutableState<() -> Unit> = mutableStateOf({})
    val onStartedHandler: MutableState<() -> Unit> = mutableStateOf({})

    var process: Shell? = null

    var subprocessId: Long? = null
    val subProcess
        get() = subprocessId?.let { spId ->
            process?.allProcesses?.firstOrNull { p ->
                p.pid() == spId
            }
        }

    var isStartingState = mutableStateOf(false)
    var isStarting: Boolean
        get() = isStartingState.value
        set(value) {
            isStartingState.value = value
        }

    val isRunningState = mutableStateOf(false)
    var isRunning: Boolean
        get() = isStartingState.value
        set(value) {
            isStartingState.value = value
        }

    // todo :  replace all params with what needed
    val cmd
        get() = exec.replace("%u", "").replace("%U", "").trim()

    val command
        get() = process?.command

    val pids: List<Long>
        get() = process?.pids ?: emptyList()

    fun start(
        api: DesktopProvider,
        scope: CoroutineScope = api.scope
    ) = runCatching {
        scope.launch {
            println("Starting app: $name [$fileName].")
            triggerStart()
            process = Shell(api)
                .writeCommand("export DBUS_SESSION_BUS_ADDRESS=\"unix:path=\$XDG_RUNTIME_DIR/bus\"")
//                .writeCommand("gtk-launch", fileName, "&")
                .writeCommand("$cmd  &")
                .writeCommand( "echo \$\$")
                .writeCommand( "exit")
                .apply {
                    triggerStarted()
                    println("app started.")
                    val subProcessString = readOutput().trim()
                    println("shell out: $subProcessString")
                    subprocessId = runCatching { subProcessString.toLong() }.getOrNull()
                    println("subprocess id : $subprocessId")
                    while (subProcess?.isAlive == true) {
                        delay(100)
                    }
                    triggerStop()
                    println("app stopped")
                }
        }
    }.onFailure { error ->
        triggerStop(error)
    }

    fun triggerStart() {
        isRunning = false
        isStarting = true
        onStartingHandler.value.invoke()
    }

    fun triggerStarted() {
        isStarting = false
        isRunning = true
        onStartedHandler.value.invoke()
    }

    fun triggerStop() = triggerStop(null)

    fun triggerStop(result: Throwable?) {
        isStarting = false
        isRunning = false
        onStopHandler.value.invoke(result ?: EmptyException)
    }

    fun onStop(function: (Throwable) -> Unit): App {
        onStopHandler.value = function
        return this
    }

    fun onStarting(function: () -> Unit): App {
        onStartingHandler.value = function
        return this
    }

    fun onStarted(function: () -> Unit): App {
        onStartedHandler.value = function
        return this
    }

    fun exit() {
        process?.close()
    }

    override fun equals(other: Any?): Boolean {
        return when {
            other is App -> {
                @Suppress("PlatformExtensionReceiverOfInline")
                other.file.name.contentEquals(file.name)
            }

            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + desktopFile.hashCode()
        result = 31 * result + isRunning.hashCode()
        return result
    }

//    fun hasWindow(api: DesktopProvider): Boolean =
//        process?.hasWindow(api) ?: false

//    fun isWindowFocus(api: DesktopProvider): Boolean =
//        process?.isWindowFocus(api) ?: false

//    fun minimizeWindow(api: DesktopProvider) =
//        process?.minimizeWindow(api) ?: false

//    fun requestWindowFocus(api: DesktopProvider) =
//        process?.requestWindowFocus(api) ?: false

    companion object {
        val Empty: App = App()

        fun List<String>?.ifEmptyCategories(block: () -> List<String>) = this?.filter { s ->
            s.isNotEmpty()
        }.let { list ->
            when {
                list == null -> block()
                list.isEmpty() -> block()
                else -> list
            }
        }
    }
}

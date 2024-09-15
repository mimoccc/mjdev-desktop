package eu.mjdev.desktop.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.helpers.EmptyException.Companion.EmptyException
import eu.mjdev.desktop.windows.WindowsTracker
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate", "HasPlatformType")
class App(
    val file: File = File("nonexistent_file_path"),
    val desktopFile: DesktopFile = DesktopFile(file),
) {
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

    var process: Process? = null
    val isRunning: Boolean
        get() = (process?.isAlive ?: false)

    var isStarted: Boolean = false

    // todo :  replace all params with what needed
    val cleanExec
        get() = exec.replace("%u", "").replace("%U", "")

    val runningProcesses
        get() = ProcessHandle.allProcesses()

    fun start() = runCatching {
        triggerStart()
        println("Starting app: $name [$cleanExec]")
        ProcessBuilder("/bin/bash", "-c", cleanExec).start().also {
            process = it
        }.apply {
            onExit().thenRun {
                triggerStop()
            }
        }
        triggerStarted()
    }.onFailure { error ->
        triggerStop(error)
    }

    fun triggerStart() {
        isStarted = true
        onStartingHandler.value.invoke()
    }

    fun triggerStarted() {
        isStarted = true
        onStartedHandler.value.invoke()
    }

    fun triggerStop() = triggerStop(null)

    fun triggerStop(result: Throwable?) {
        isStarted = false
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

    fun requestWindowFocus() {
        WindowsTracker().getWindowByPid(process?.pid())?.toFront() ?: closeWindow()
    }

    fun minimizeWindow() {
        WindowsTracker().getWindowByPid(process?.pid())?.minimize()
    }

    fun maximizeWindow() {
        WindowsTracker().getWindowByPid(process?.pid())?.maximize()
    }

    fun closeWindow() {
        WindowsTracker().getWindowByPid(process?.pid())?.close() ?: exit()
    }

    fun exit() {
        process?.destroy()
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

package eu.mjdev.desktop.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.helpers.exception.EmptyException.Companion.EmptyException
import eu.mjdev.desktop.provider.DesktopProvider
import java.io.File
import kotlin.jvm.optionals.getOrNull

val Process.command: String?
    get() = info().command().getOrNull()

@Suppress("unused", "MemberVisibilityCanBePrivate", "HasPlatformType")
class App(
    val file: File = File("nonexistent_file_path"),
    val desktopFile: DesktopFile = DesktopFile(file),
    val isFavorite: Boolean = false
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
        get() = (process?.isAlive ?: false) || process?.children()?.anyMatch { it.isAlive } == true

    var isStarted: Boolean = false

    // todo :  replace all params with what needed
    val cmd
        get() = exec.replace("%u", "").replace("%U", "").trim()

    val command
        get() = process?.command

    val pids: List<Long>
        get() = getProcessPids(process?.toHandle())

    val runningProcesses
        get() = ProcessHandle.allProcesses()

    fun getProcessPids(p: ProcessHandle?): List<Long> {
        val pids = mutableListOf<Long>()
        if (p != null) {
            pids.add(p.pid())
            p.children().forEach { pch ->
                pids.addAll(getProcessPids(pch))
            }
        }
        return pids
    }

    fun start() = runCatching {
        triggerStart()
        println("Starting app: $name [$cmd]")
        ProcessBuilder("/bin/bash", "-c", cmd).start().also {
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

    fun isWindowFocus(api: DesktopProvider): Boolean =
        api.windows.isWindowActive(pids)

    fun hasWindow(api: DesktopProvider) =
        api.windows.getWindowsByPids(pids).isNotEmpty()

    fun requestWindowFocus(api: DesktopProvider) {
        val windows = api.windows.getWindowsByPids(pids)
        if (windows.isNotEmpty()) {
            println("Got windows:${windows.size}")
            println("$windows")
            windows.forEach { w -> w.toFront() }
        } else {
            closeWindow(api)
        }
    }

    fun minimizeWindow(api: DesktopProvider) {
        val windows = api.windows.getWindowsByPids(pids)
        if (windows.isNotEmpty()) {
            windows.forEach { w -> w.minimize() }
        } else {
            closeWindow(api)
        }
    }

    fun maximizeWindow(api: DesktopProvider) {
        val windows = api.windows.getWindowsByPids(pids)
        if (windows.isNotEmpty()) {
            windows.forEach { w -> w.maximize() }
        } else {
            closeWindow(api)
        }
    }

    fun closeWindow(api: DesktopProvider) {
        val windows = api.windows.getWindowsByPids(pids)
        if (windows.isNotEmpty()) {
            windows.forEach { w -> w.close() }
        } else {
            exit()
        }
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

package eu.mjdev.desktop.data

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.extensions.Custom.command
import eu.mjdev.desktop.extensions.Custom.commandLine
import eu.mjdev.desktop.helpers.exception.EmptyException.Companion.EmptyException
import eu.mjdev.desktop.helpers.system.Shell
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate")
class App(
    val file: File? = null,
    val desktopFile: DesktopFile? = runCatching { file?.let { f -> DesktopFile(f) } }.getOrNull(),
    val isFavorite: Boolean = false
) {
    val fileName
        get() = desktopFile?.fileName
    val fullAppName
        get() = desktopFile?.fullAppName ?: ""
    val type
        get() = desktopFile?.desktopSection?.Type ?: ""
    val version
        get() = desktopFile?.desktopSection?.Version ?: ""
    val name
        get() = desktopFile?.desktopSection?.Name ?: ""
    val comment
        get() = desktopFile?.desktopSection?.Comment ?: ""
    val path
        get() = desktopFile?.desktopSection?.Path ?: ""
    val exec
        get() = desktopFile?.desktopSection?.Exec ?: ""
    val iconName
        get() = desktopFile?.desktopSection?.Icon ?: ""
    val categories
        get() = desktopFile?.desktopSection?.Categories.ifEmptyCategories { listOf(Category.UNCATEGORIZED) }
    val notifyOnStart
        get() = desktopFile?.desktopSection?.NotifyOnStart ?: false
    val runInTerminal
        get() = desktopFile?.desktopSection?.RunInTerminal ?: false
    val windowClass
        get() = desktopFile?.desktopSection?.StartupWMClass.orEmpty().ifEmpty { fullAppName }

    val cmd
        get() = exec.split(" ").first()

    val hasProcess: Boolean = ProcessHandle.allProcesses().toList().any { ph ->
        ph.command.contentEquals(windowClass, true) ||
                ph.command.contains(cmd, true) ||
                ph.command.contains(name, true) ||
                ph.commandLine.contentEquals(windowClass, true) ||
                ph.commandLine.contains(cmd, true) ||
                ph.commandLine.contains(name, true)
    }

    var iconTint: Color? = null
    val iconBackground: Color = Color.White

    val onStopHandler: MutableState<(Throwable) -> Unit> = mutableStateOf({})
    val onStartingHandler: MutableState<() -> Unit> = mutableStateOf({})
    val onStartedHandler: MutableState<() -> Unit> = mutableStateOf({})

    var isStartingState = mutableStateOf(false)
    var isRunningState = mutableStateOf(false)
    var isStarting: Boolean
        get() = isStartingState.value
        internal set(value) {
            isStartingState.value = value
        }
    var isRunning: Boolean
        get() = isStartingState.value
        internal set(value) {
            isStartingState.value = value
        }

    fun start() = runCatching {
        println("Starting app: $name [$windowClass].")
        println("dex -w ${desktopFile?.absolutePath}")
        triggerStart()
        Shell {
            startApp(
                app = this@App,
                onStarted = {
                    println("App started app: $name [$windowClass].")
                    triggerStarted()
                },
                onStopped = { e ->
                    println("App stopped: $name [$windowClass].")
                    triggerStop(e)
                }
            )
        }
    }.onFailure { e ->
        triggerStop(e)
    }

    fun triggerStart() {
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

    override fun equals(other: Any?): Boolean {
        return when {
            other is App -> {
                other.file?.name?.contentEquals(file?.name) ?: false
            }

            else -> false
        }
    }

//    fun hasWindow(api: DesktopProvider): Boolean =
//        api.windowsManager.hasWindow(this)

    override fun hashCode(): Int {
        var result = file?.hashCode() ?: 0
        result = 31 * result + (desktopFile?.hashCode() ?: 0)
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + (fileName?.hashCode() ?: 0)
        result = 31 * result + fullAppName.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + comment.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + exec.hashCode()
        result = 31 * result + iconName.hashCode()
        result = 31 * result + categories.hashCode()
        result = 31 * result + notifyOnStart.hashCode()
        result = 31 * result + runInTerminal.hashCode()
        result = 31 * result + windowClass.hashCode()
        result = 31 * result + cmd.hashCode()
        result = 31 * result + hasProcess.hashCode()
        result = 31 * result + (iconTint?.hashCode() ?: 0)
        result = 31 * result + iconBackground.hashCode()
        result = 31 * result + isStarting.hashCode()
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

        @Composable
        fun rememberRunningIndicator(app: App?) = remember(
            app,
            app?.isFavorite,
            app?.isRunning,
            app?.isStarting
        ) {
            RunningAppIndicator(app)
        }

        // todo
        class RunningAppIndicator(
            val app: App?
        ) : State<Boolean> {
            override val value: Boolean
                get() = app?.isRunning == true || app?.hasProcess == true
        }
    }
}

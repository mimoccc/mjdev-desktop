package eu.mjdev.desktop.data

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.helpers.exception.EmptyException.Companion.EmptyException
import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate")
class App(
    val desktopFile: DesktopFile = DesktopFile.Empty,
    val isFavorite: Boolean = false
) {
    constructor(file: File) : this(DesktopFile(file))

    val file: File get() = desktopFile.file

    val fileName: String
        get() = desktopFile.fileName
    val fullAppName: String
        get() = desktopFile.fullAppName
    val type: DesktopFile.DesktopEntryType
        get() = desktopFile.desktopSection?.Type ?: DesktopFile.DesktopEntryType.Application
    val version: String
        get() = desktopFile.desktopSection?.Version.orEmpty()
    val name: String
        get() = desktopFile.desktopSection?.Name ?: fullAppName
    val comment: String
        get() = desktopFile.desktopSection?.Comment ?: fullAppName
    val path: String
        get() = desktopFile.desktopSection?.Path.orEmpty()
    val exec: String
        get() = desktopFile.desktopSection?.Exec.orEmpty()
    val iconName: String
        get() = desktopFile.desktopSection?.Icon ?: fullAppName
    val categories: List<String>
        get() = desktopFile.desktopSection?.Categories.orEmpty().ifEmptyCategories { listOf(Category.UNCATEGORIZED) }
    val notifyOnStart: Boolean
        get() = desktopFile.desktopSection?.NotifyOnStart ?: false
    val runInTerminal: Boolean
        get() = desktopFile.desktopSection?.RunInTerminal ?: false
    val windowClass: String
        get() = desktopFile.desktopSection?.StartupWMClass.orEmpty().ifEmpty { fullAppName }

    val cmd
        get() = exec.split(" ").first()

    val desktopData
        get() = desktopFile.fileData

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

    fun start(
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) = scope.launch(Dispatchers.IO) {
        runCatching {
            println("Starting app: $name [$windowClass].")
            println("dex -w ${desktopFile.absolutePath}")
            triggerStart()
            Shell {
                startApp(
                    app = this@App,
                    onStarted = {
                        println("App started app: $name [$windowClass].")
                        println("Desktop File:")
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
                other.fileName.contentEquals(file.name)
            }

            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + desktopFile.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + fileName.hashCode()
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
        fun rememberRunningIndicator(
            app: App?,
            api: DesktopProvider = LocalDesktop.current
        ) = remember(
            app,
            app?.isFavorite,
            app?.isRunning,
            app?.isStarting
        ) {
            RunningAppIndicator(api, app)
        }

        // todo
        class RunningAppIndicator(
            val api: DesktopProvider,
            val app: App?
        ) : State<Boolean> {
            override val value: Boolean
                get() = app?.isRunning == true || api.processManager.hasAppProcess(app)
        }
    }
}

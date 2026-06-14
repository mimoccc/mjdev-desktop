package org.mjdev.desktop.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.Path
import org.mjdev.desktop.extensions.PathExt.text
import org.mjdev.desktop.helpers.exception.EmptyException.Companion.EmptyException
import org.mjdev.desktop.helpers.system.shell.Shell
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.log.Log

@Suppress("unused", "MemberVisibilityCanBePrivate")
class App(
    val desktopFile: DesktopFile = DesktopFile.Empty,
    val isFavorite: Boolean = false,
) : IApp {
    constructor(file: Path) : this(DesktopFile(file))

    val file: Path get() = desktopFile.file

    override val fullTextString: String
        get() = desktopFile.fileData

    val fileName: String
        get() = desktopFile.fileName
    override val fullAppName: String
        get() = desktopFile.fullAppName
    val type: DesktopEntryType
        get() = desktopFile.desktopSection?.Type ?: DesktopEntryType.Application
    val version: String
        get() = desktopFile.desktopSection?.Version.orEmpty()
    override val name: String
        get() = desktopFile.desktopSection?.Name ?: fullAppName
    override val comment: String
        get() = desktopFile.desktopSection?.Comment ?: fullAppName
    val path: String
        get() = desktopFile.desktopSection?.Path.orEmpty()
    val exec: String
        get() = desktopFile.desktopSection?.Exec.orEmpty()
    val iconName: String
        get() = desktopFile.desktopSection?.Icon ?: fullAppName
    override val categories: List<Category>
        get() =
            desktopFile.desktopSection
                ?.Categories
                .orEmpty()
                .map {
                    Category(it)
                }.ifEmpty {
                    listOf(Category.Empty)
                }
    val notifyOnStart: Boolean
        get() = desktopFile.desktopSection?.NotifyOnStart ?: false
    val runInTerminal: Boolean
        get() = desktopFile.desktopSection?.RunInTerminal ?: false
    val windowClass: String
        get() =
            desktopFile.desktopSection
                ?.StartupWMClass
                .orEmpty()
                .ifEmpty { fullAppName }

    override val cmd
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

    override var isStarting: Boolean
        get() = isStartingState.value
        internal set(value) {
            isStartingState.value = value
        }

    override var isRunning: Boolean
        get() = isStartingState.value
        internal set(value) {
            isStartingState.value = value
        }

    override suspend fun start() {
        runCatching {
            Log.i("Starting app: $name [$windowClass].")
            Log.i("dex -w ${desktopFile.absolutePath}")
            triggerStart()
            Shell {
                startApp(
                    app = this@App,
                    onStarted = {
                        Log.i("App started app: $name [$windowClass].")
                        Log.i("Desktop File:")
                        triggerStarted()
                    },
                    onStopped = { e ->
                        Log.i("App stopped: $name [$windowClass].")
                        triggerStop(e)
                    },
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

    override fun equals(other: Any?): Boolean =
        when {
            other is App -> {
                other.fileName.contentEquals(file.name)
            }

            else -> false
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

    override fun toString(): String = name

    companion object {
        val Test =
            App(
                desktopFile = DesktopFile.Test,
                isFavorite = true,
            ).apply {
                isStartingState.value = true
            }

        val Empty: App = App()

        fun List<String>?.ifEmptyCategories(block: () -> List<String>) =
            this
                ?.filter { s ->
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

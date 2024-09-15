package eu.mjdev.desktop.provider.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.helpers.EmptyException.Companion.EmptyException
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.io.File
import kotlin.reflect.full.companionObjectInstance

@Suppress("unused", "MemberVisibilityCanBePrivate")
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
    val iconBackground: Color
        get() : Color = runCatching { colorFromName() }.getOrNull() ?: Color.White

    val onStopHandler: MutableState<(Throwable) -> Unit> = mutableStateOf({})
    val onStartHandler: MutableState<() -> Unit> = mutableStateOf({})

    var process: Process? = null
    val isRunning: Boolean get() = process?.isAlive ?: false

    // todo :  replace all params with what needed
    fun start() = runCatching {
        val cleanExec = exec.replace("%u", "").replace("%U", "")
        println("Starting app: $name [$cleanExec]")
        ProcessBuilder("/bin/bash", "-c", cleanExec).start().also {
            process = it
        }.onExit().thenRun {
            onStopHandler.value.invoke(EmptyException)
        }
    }.onFailure { error ->
        onStopHandler.value.invoke(error)
    }.onSuccess {
        onStartHandler.value.invoke()
    }

    private fun colorFromName(): Color {
        val companion = Color::class.companionObjectInstance
        val members = if (companion != null) companion::class.members.toList() else emptyList()
        return members.map {
            Pair(FuzzySearch.ratio(name, it.name), it)
        }.maxByOrNull { it.first }?.second?.call(null) as? Color ?: Color.White
    }

    fun onStop(function: (Throwable) -> Unit): App {
        onStopHandler.value = function
        return this
    }

    fun onStart(function: () -> Unit): App {
        onStartHandler.value = function
        return this
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

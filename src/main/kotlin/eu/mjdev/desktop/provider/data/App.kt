package eu.mjdev.desktop.provider.data

import androidx.compose.ui.graphics.Color
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.io.File
import kotlin.reflect.full.companionObjectInstance

@Suppress("unused", "MemberVisibilityCanBePrivate")
class App(
    val file: File? = null,
    val desktopFile: DesktopFile? = file?.let { DesktopFile(file) }
) {
    val type
        get() = desktopFile?.type ?: "Application"
    val version
        get() = desktopFile?.version ?: ""
    val name
        get() = desktopFile?.name ?: ""
    val comment
        get() = desktopFile?.comment ?: ""
    val path
        get() = desktopFile?.path ?: ""
    val exec
        get() = desktopFile?.exec ?: ""
    val iconName
        get() = desktopFile?.icon ?: ""
    val categories
        get() = desktopFile?.categories.ifEmptyCategories { listOf(Category.UNCATEGORIZED) }
    val notifyOnStart
        get() = desktopFile?.notifyOnStart ?: false
    val runInTerminal
        get() = desktopFile?.runInTerminal ?: false

    var enabled: Boolean = true
    var iconTint: Color? = null

    val iconBackground: Color
        get() : Color = runCatching { colorFromName() }.getOrNull() ?: Color.White

    // todo :  replace all params with what needed
    fun start() = runCatching {
        val cleanExec = exec.replace("%u", "").replace("%U", "")
        println("Starting app: $name [$cleanExec]")
        ProcessBuilder("/bin/bash", "-c", cleanExec).start()
    }.onFailure { error ->
        error.printStackTrace()
    }

    private fun colorFromName(): Color {
        val companion = Color::class.companionObjectInstance
        val members = if (companion != null) companion::class.members.toList() else emptyList()
        return members.map {
            Pair(FuzzySearch.ratio(name, it.name), it)
        }.maxByOrNull { it.first }?.second?.call(null) as? Color ?: Color.White
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

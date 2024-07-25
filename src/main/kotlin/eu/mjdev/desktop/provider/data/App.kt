package eu.mjdev.desktop.provider.data

import androidx.compose.ui.graphics.Color
import java.io.File

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

    fun start() = runCatching {
        ProcessBuilder("/bin/bash", "-c", exec).start()
    }.onFailure { error -> error.printStackTrace() }

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

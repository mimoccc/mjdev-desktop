package eu.mjdev.desktop.data

import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.helpers.backgrounds.ProviderErzvo
import eu.mjdev.desktop.helpers.backgrounds.ProviderLocal
import eu.mjdev.desktop.helpers.backgrounds.ProviderSmug
import eu.mjdev.desktop.helpers.internal.ImagesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("MemberVisibilityCanBePrivate", "unused")
class DesktopConfig(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    val block: DesktopConfig.() -> Unit = {}
) {
    val desktopBackgrounds = mutableListOf<Any>()

    init {
        block()
    }

    fun addBackground(path: String) {
        desktopBackgrounds.add(path)
    }

    fun addBackground(color: Color) {
        desktopBackgrounds.add(color)
    }

    @Suppress("UNCHECKED_CAST")
    fun addBackground(
        provider: suspend () -> Any?,
    ) = scope.launch(Dispatchers.IO) {
        provider().also { bcks ->
            when (bcks) {
                null -> Unit
                is Collection<*> -> desktopBackgrounds.addAll(bcks as Collection<Any>)
                else -> desktopBackgrounds.add(bcks)
            }
        }
    }

    private fun addBackground(
        provider: ImagesProvider
    ) = addBackground {
        provider.get()
    }

    companion object {
        val configCache = mutableMapOf<String, DesktopConfig>()

        private val DEFAULT = DesktopConfig {
//            addBackground(ProviderSmug())
//            addBackground(ProviderErzvo())
        }

        // todo load from user settings
        fun load(
            user: User
        ): DesktopConfig = configCache[user.userName] ?: DEFAULT.apply {
            configCache[user.userName] = this
            addBackground(ProviderLocal(user))
        }
    }
}

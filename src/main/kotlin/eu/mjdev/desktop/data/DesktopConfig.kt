package eu.mjdev.desktop.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.helpers.backgrounds.ProviderErzvo
import eu.mjdev.desktop.helpers.backgrounds.ProviderSmug
import eu.mjdev.desktop.helpers.internal.ImagesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("MemberVisibilityCanBePrivate", "unused")
class DesktopConfig(
    val block: DesktopConfig.() -> Unit = {}
) {
    val desktopBackgroundsState = mutableStateListOf<Any>()
    val desktopBackgrounds get() = desktopBackgroundsState

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
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        provider: suspend () -> Any?,
    ) = scope.launch {
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
        val Default = DesktopConfig {
            addBackground(ProviderSmug())
            addBackground(ProviderErzvo())
//            addBackground(ProviderLocal(api))
        }
    }
}

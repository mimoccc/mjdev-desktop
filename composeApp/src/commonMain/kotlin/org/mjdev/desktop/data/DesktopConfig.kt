package org.mjdev.desktop.data

import androidx.compose.ui.graphics.Color
import org.mjdev.desktop.helpers.compose.ImagesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.providers.background.ProviderErzvo
import org.mjdev.desktop.providers.background.ProviderLocal
import org.mjdev.desktop.providers.background.ProviderSmug

@Suppress("MemberVisibilityCanBePrivate", "unused")
class DesktopConfig(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val block: DesktopConfig.() -> Unit = {}
) {
    val desktopBackgrounds = mutableListOf<Any>()
    var theme: ITheme = ITheme.DEFAULT

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
    ) = scope.launch(Dispatchers.Default) {
        provider().also { p ->
            when (p) {
                null -> Unit
                is Collection<*> -> desktopBackgrounds.addAll(p as Collection<Any>)
                else -> desktopBackgrounds.add(p)
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
            addBackground(ProviderErzvo())
        }

        // todo load from user settings
        fun load(
            user: IUser
        ): DesktopConfig = configCache[user.userName] ?: DEFAULT.apply {
            theme = ITheme.load(user)
            configCache[user.userName] = this
            addBackground(ProviderLocal(user))
        }
    }
}

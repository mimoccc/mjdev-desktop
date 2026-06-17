package org.mjdev.desktop.data

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.desktop.helpers.compose.ImagesProvider
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser

@Suppress("MemberVisibilityCanBePrivate", "unused")
class DesktopConfig(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val block: DesktopConfig.() -> Unit = {},
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
    fun addBackground(provider: suspend () -> Any?) =
        scope.launch(Dispatchers.Default) {
            provider().also { p ->
                when (p) {
                    null -> Unit
                    is Collection<*> -> desktopBackgrounds.addAll(p as Collection<Any>)
                    else -> desktopBackgrounds.add(p)
                }
            }
        }

    fun addBackground(provider: ImagesProvider) =
        addBackground {
            provider.get()
        }

    /** Clears the current wallpapers and re-populates them from the given [providers]. */
    fun reloadBackgrounds(providers: List<ImagesProvider>) {
        desktopBackgrounds.clear()
        providers.forEach { provider -> addBackground(provider) }
    }

    companion object {
        val configCache = mutableMapOf<String, DesktopConfig>()

        private val DEFAULT = DesktopConfig {}

        // Loads the per-user desktop config from ~/.mjdev/desktop/config.json (defaults on first
        // run), applies the persisted tunables to the theme and populates the enabled background
        // providers.
        fun load(user: IUser): DesktopConfig =
            configCache[user.userName] ?: DEFAULT.apply {
                val themeForUser = ITheme.load(user)
                val configData = DesktopConfigStore(user).load()
                configData.applyTo(themeForUser)
                theme = themeForUser
                configCache[user.userName] = this
                reloadBackgrounds(configData.buildProviders(user))
            }
    }
}

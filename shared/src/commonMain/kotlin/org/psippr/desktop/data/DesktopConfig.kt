package org.psippr.desktop.data

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.psippr.desktop.helpers.compose.ImagesProvider
import org.psippr.desktop.helpers.property.ObservableProperty
import org.psippr.desktop.interfaces.ITheme
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.log.Log

@Suppress("MemberVisibilityCanBePrivate", "unused")
class DesktopConfig(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val block: DesktopConfig.() -> Unit = {},
) {
    // old, not observable and filled from a background thread - kept for compatibility,
    // listen to [backgroundsProperty] instead
    val desktopBackgrounds = mutableListOf<Any>()

    /** Wallpapers that can be listened to; replaced as a whole when a provider scan finishes. */
    val backgroundsProperty = ObservableProperty<List<Any>>(emptyList())

    /** Current wallpapers, backed by [backgroundsProperty]. */
    var backgrounds: List<Any> by backgroundsProperty

    var theme: ITheme = ITheme.DEFAULT

    init {
        block()
    }

    fun addBackground(path: String) = appendBackgrounds(listOf(path))

    fun addBackground(color: Color) = appendBackgrounds(listOf(color))

    fun addBackground(provider: suspend () -> Any?) = scope.launch(Dispatchers.Default) {
        appendBackgrounds(provider().toBackgrounds())
    }

    fun addBackground(provider: ImagesProvider) = addBackground {
        provider.get()
    }

    /**
     * Re-populates the wallpapers from the given [providers]. All providers are read first and
     * the result is published once, so listeners never see a temporarily cleared list.
     */
    fun reloadBackgrounds(providers: List<ImagesProvider>): Job = scope.launch(Dispatchers.Default) {
        publishBackgrounds(providers.flatMap { provider -> provider.loadBackgrounds() })
    }

    private suspend fun ImagesProvider.loadBackgrounds(): List<Any> = runCatching { get().toBackgrounds() }
        .onFailure { e -> Log.e(e) }
        .getOrDefault(emptyList())

    private fun appendBackgrounds(items: List<Any>) {
        if (items.isEmpty()) return
        backgroundsProperty.update { current -> current + items }
        syncLegacyBackgrounds()
    }

    private fun publishBackgrounds(items: List<Any>) {
        backgrounds = items
        syncLegacyBackgrounds()
    }

    private fun syncLegacyBackgrounds() {
        desktopBackgrounds.clear()
        desktopBackgrounds.addAll(backgrounds)
    }

    private fun Any?.toBackgrounds(): List<Any> = when (this) {
        null -> emptyList()
        is Collection<*> -> filterNotNull()
        else -> listOf(this)
    }

    companion object {
        val configCache = mutableMapOf<String, DesktopConfig>()

        // old shared instance - load() used to reuse it for every user, so loading a second user
        // cleared the first user's wallpapers; kept for compatibility, no longer used by load()
        private val DEFAULT = DesktopConfig {}

        // Loads the per-user desktop config from ~/.psippr-desktop/desktop/config.json (defaults on first
        // run), applies the persisted tunables to the theme and populates the enabled background
        // providers. Every user gets an own instance.
        fun load(user: IUser): DesktopConfig = configCache[user.userName] ?: DesktopConfig().apply {
            val themeForUser = ITheme.load(user)
            val configData = DesktopConfigStore(user).load()
            configData.applyTo(themeForUser)
            theme = themeForUser
            configCache[user.userName] = this
            reloadBackgrounds(configData.buildProviders(user))
        }
    }
}

package org.psippr.desktop.data

import org.psippr.desktop.helpers.compose.ImagesProvider
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.providers.background.ProviderBackgroundFolders
import org.psippr.desktop.providers.background.ProviderErzvo
import org.psippr.desktop.providers.background.ProviderSmug

/**
 * Persisted configuration of a single background [ImagesProvider]. Stored in the desktop
 * config JSON so the user can enable/disable and tune each provider from the control center.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
data class ProviderConfig(
    // Default value is required: it gives Gson a no-args constructor, otherwise it falls back to
    // sun.misc.Unsafe which is unavailable on some JVMs and crashed config loading.
    val id: String = "",
    var enabled: Boolean = false,
    var loadCount: Int = DEFAULT_LOAD_COUNT,
) {
    /** Human-readable label for the provider, resolved from its [id]. */
    val label: String
        get() = Provider.byId(id)?.label ?: id

    /** Whether this provider exposes a tunable load-count (remote providers do, local does not). */
    val hasLoadCount: Boolean
        get() = Provider.byId(id)?.hasLoadCount ?: false

    /** Builds the live [ImagesProvider] for this config, or null when disabled / unknown. */
    fun build(
        user: IUser,
        excludedFolders: Set<String> = emptySet(),
    ): ImagesProvider? {
        if (!enabled) return null
        return when (Provider.byId(id)) {
            Provider.Local -> ProviderBackgroundFolders(user, excludedFolders)
            Provider.Smug -> ProviderSmug(loadCount)
            Provider.Erzvo -> ProviderErzvo(loadCount)
            null -> null
        }
    }

    /** Registry of the background providers the desktop knows how to construct. */
    enum class Provider(
        val id: String,
        val label: String,
        val hasLoadCount: Boolean,
    ) {
        Local("local", "Local folder", false),
        Smug("smug", "WallpaperSmug", true),
        Erzvo("erzvo", "Erzvo", true),
        ;

        companion object {
            fun byId(id: String): Provider? = entries.firstOrNull { it.id == id }
        }
    }

    companion object {
        const val DEFAULT_LOAD_COUNT = 10

        /** The default provider set: local backgrounds on, remote providers off. */
        fun defaults(): MutableList<ProviderConfig> = Provider.entries
            .map { provider ->
                ProviderConfig(
                    id = provider.id,
                    enabled = provider == Provider.Local,
                )
            }.toMutableList()
    }
}

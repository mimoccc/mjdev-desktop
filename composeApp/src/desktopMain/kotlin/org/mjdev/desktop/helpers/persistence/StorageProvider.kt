package org.mjdev.desktop.helpers.persistence

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.resources.Res
import org.mjdev.desktop.resources.app_id
import java.util.prefs.Preferences

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual open class StorageProvider actual constructor(
    context: IDesktopContext
) {
    private val prefNodeName
        get() = runBlocking {
            getString(Res.string.app_id)
        }
    private val prefs by lazy {
        Preferences.userRoot().node(prefNodeName)
    }

    actual open fun put(
        key: String,
        value: String
    ) = prefs.put(key, value)

    actual open fun get(
        key: String,
        default: String?
    ) = prefs.get(key, default)

    actual open fun remove(
        key: String
    ) = prefs.remove(key)

    actual open fun clear() =
        prefs.clear()

    actual open fun getAll(): Map<String, String> =
        prefs.keys().associate { key ->
            prefs.get(key, null).let { value ->
                key to value
            }
        }

}
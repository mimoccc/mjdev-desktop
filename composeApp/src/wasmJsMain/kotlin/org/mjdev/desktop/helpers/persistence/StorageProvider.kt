package org.mjdev.desktop.helpers.persistence

import org.mjdev.desktop.context.IDesktopContext

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual open class StorageProvider actual constructor(
    context: IDesktopContext
) {
    private val prefs by lazy {
        HashMap<String, String>() // todo
    }

    actual open fun put(
        key: String,
        value: String
    ) {
        prefs[key] = value
    }

    actual open fun get(
        key: String,
        default: String?
    ) = prefs[key] ?: default

    actual open fun remove(
        key: String
    ) {
        prefs.remove(key)
    }

    actual open fun clear() {
        prefs.clear()
    }

    actual open fun getAll(): Map<String, String> =
        prefs.keys.associate { key ->
            prefs[key].let { value ->
                key to (value ?: "")
            }
        }

}
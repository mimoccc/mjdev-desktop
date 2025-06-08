package org.mjdev.desktop.helpers.persistence

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object StorageProvider {
    private val prefs by lazy {
        HashMap<String, String>() // todo
    }

    actual val instance: KeyValueStorage = object : KeyValueStorage {
        override fun put(
            key: String,
            value: String
        ) {
            prefs[key] = value
        }

        override fun get(
            key: String,
            default: String?
        ) = prefs.get(key) ?: default

        override fun remove(
            key: String
        ) {
            prefs.remove(key)
        }

        override fun clear() {
            prefs.clear()
        }
    }
}
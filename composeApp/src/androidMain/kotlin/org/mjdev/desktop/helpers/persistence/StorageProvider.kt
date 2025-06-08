package org.mjdev.desktop.helpers.persistence

import android.content.ContextWrapper
import android.app.Application
import org.mjdev.desktop.BuildConfig

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object StorageProvider {
    private val prefs by lazy {
        ContextWrapper(Application()).getSharedPreferences(BuildConfig.APPLICATION_ID, 0)
    }

    actual val instance: KeyValueStorage = object : KeyValueStorage {
        override fun put(
            key: String,
            value: String
        ) {
            prefs.edit().putString(key, value).apply()
        }

        override fun get(
            key: String,
            default: String?
        ) = prefs.getString(key, default)

        override fun remove(
            key: String
        ) {
            prefs.edit().remove(key).apply()
        }

        override fun clear() {
            prefs.edit().clear().apply()
        }
    }
}
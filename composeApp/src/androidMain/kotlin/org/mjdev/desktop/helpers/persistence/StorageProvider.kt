package org.mjdev.desktop.helpers.persistence

import org.mjdev.desktop.resources.Res
import androidx.core.content.edit
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.mjdev.desktop.context.DesktopContext
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.resources.app_id

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual open class StorageProvider actual constructor(
    val context: IDesktopContext
) {
    private val applicationContext
        get() = (context as? DesktopContext)?.androidContext?.applicationContext

    private val prefs by lazy {
        runBlocking {
            val appId = getString(Res.string.app_id)
            applicationContext?.getSharedPreferences(appId, 0)
        }
    }

    actual open fun put(
        key: String,
        value: String
    ) = prefs?.edit {
        putString(key, value)
    } ?: throw IllegalStateException("SharedPreferences not initialized")

    actual open fun get(
        key: String,
        default: String?
    ) : String? = prefs?.getString(
        key,
        default
    ) ?: throw IllegalStateException("SharedPreferences not initialized")

    actual open fun remove(
        key: String
    ) = prefs?.edit {
        remove(key)
    } ?: throw IllegalStateException("SharedPreferences not initialized")

    actual open fun clear() = prefs?.edit {
        clear()
    } ?: throw IllegalStateException("SharedPreferences not initialized")

    actual open fun getAll(): Map<String, String> =
        prefs?.all?.mapValues { entry ->
            entry.value?.toString() ?: ""
        } ?: emptyMap()

}
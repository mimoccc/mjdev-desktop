package org.mjdev.desktop.components.controlcenter.base

import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.CustomExt.asJson
import org.mjdev.desktop.extensions.CustomExt.to
import org.mjdev.desktop.interfaces.IControlCenterPageDataSaver
import org.mjdev.desktop.log.Log

class PersistentPageSaver(
    val context: IDesktopContext,
    override val key: String,
) : IControlCenterPageDataSaver {
    override fun save(data: Map<Int, Any>) {
        Log.d("Saving data for $key: $data")
        context.storageProvider.put(key, data.asJson())
    }

    override fun load(): Map<Int, Any> =
        runCatching {
            Log.d("Loading data for $key")
            context.storageProvider.get(key)?.to<Map<Int, Any>>()
        }.onFailure { e ->
            Log.e(e)
        }.getOrNull() ?: emptyMap<Int, Any>().also { data ->
            if (data.isEmpty()) {
                Log.d("No data found for $key, returning empty map.")
            } else {
                Log.d("Loaded data for $key: $data.")
            }
        }
}

package org.psippr.desktop.components.controlcenter.base

import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.extensions.CustomExt.asJson
import org.psippr.desktop.extensions.CustomExt.to
import org.psippr.desktop.interfaces.IControlCenterPageDataSaver
import org.psippr.desktop.log.Log

class PersistentPageSaver(
    val context: IDesktopContext,
    override val key: String,
) : IControlCenterPageDataSaver {
    override fun save(data: Map<Int, Any>) {
        Log.d("Saving data for $key: $data")
        context.storageProvider.put(key, data.asJson())
    }

    override fun load(): Map<Int, Any> = runCatching {
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

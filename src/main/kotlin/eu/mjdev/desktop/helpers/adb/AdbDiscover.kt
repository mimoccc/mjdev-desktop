package eu.mjdev.desktop.helpers.adb

import eu.mjdev.desktop.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Suppress("MemberVisibilityCanBePrivate", "unused")
class AdbDiscover(
    coroutineScope: CoroutineScope? = null,
    discoverDelay: Long = 1000L,
    onAdded: suspend (IAdb) -> Unit,
    onRemoved: suspend (IAdb) -> Unit,
) {
    val devices = mutableListOf<IAdb>()

    init {
        coroutineScope?.launch {
            while (isActive) {
                runCatching {
                    val newDevices: List<IAdb> = IAdb.discover().toList()
                    val removed = devices.filter { d -> !newDevices.contains(d) }
                    val added = newDevices.filter { d -> !devices.contains(d) }
                    removed.forEach { d ->
                        devices.remove(d)
                        onRemoved(d)
                    }
                    added.forEach { d ->
                        devices.add(d)
                        onAdded(d)
                    }
                }.onFailure { e ->
                    Log.e(e)
                }
                delay(discoverDelay)
            }
        }
    }

    companion object {
        fun adbDevicesHandler(
            coroutineScope: CoroutineScope? = null,
            onRemoved: suspend (IAdb) -> Unit = {},
            onAdded: suspend (IAdb) -> Unit = {},
        ) = AdbDiscover(
            coroutineScope = coroutineScope,
            onAdded = onAdded,
            onRemoved = onRemoved
        )
    }
}
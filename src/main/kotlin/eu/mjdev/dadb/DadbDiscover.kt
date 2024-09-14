package eu.mjdev.dadb

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("MemberVisibilityCanBePrivate")
class AdbDiscover(
    coroutineScope: CoroutineScope? = null,
    discoverDelay: Long = 1000L,
    onAdded: suspend (Dadb) -> Unit,
//    onRemoved: suspend (Dadb) -> Unit,
) {
    val devices = mutableMapOf<String, Dadb>()

    init {
        coroutineScope?.launch {
            while (true) {
                val newDevices = Dadb.discover()
                if (newDevices.isNotEmpty()) {
                    newDevices.forEach { entry ->
                        if (!devices.containsKey(entry.toString())) {
                            devices[entry.toString()] = entry
                            onAdded(entry)
                        }
                    }
                }
                delay(discoverDelay)
            }
        }
    }

    companion object {
        fun adbDevicesHandler(
            coroutineScope: CoroutineScope? = null,
//            onRemoved: suspend (Dadb) -> Unit = {},
            onAdded: suspend (Dadb) -> Unit = {},
        ) = AdbDiscover(
            coroutineScope = coroutineScope,
            onAdded = onAdded,
//            onRemoved = onRemoved
        )
    }
}
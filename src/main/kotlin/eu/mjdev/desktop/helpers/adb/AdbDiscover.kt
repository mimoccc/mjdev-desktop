package eu.mjdev.desktop.helpers.adb

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("MemberVisibilityCanBePrivate")
class AdbDiscover(
    coroutineScope: CoroutineScope? = null,
    discoverDelay: Long = 1000L,
    onAdded: suspend (Adb) -> Unit,
//    onRemoved: suspend (Dadb) -> Unit,
) {
    val devices = mutableMapOf<String, Adb>()

    init {
        coroutineScope?.launch {
            while (true) {
                val newDevices = Adb.discover()
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
            onAdded: suspend (Adb) -> Unit = {},
        ) = AdbDiscover(
            coroutineScope = coroutineScope,
            onAdded = onAdded,
//            onRemoved = onRemoved
        )
    }
}